package cn.gdeiassistant.Service.Photograph;

import cn.gdeiassistant.Pojo.Entity.Photograph;
import cn.gdeiassistant.Pojo.Entity.PhotographComment;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Photograph.PhotographMapper;
import cn.gdeiassistant.Service.UserLogin.UserCertificateService;
import cn.gdeiassistant.Tools.SpringUtils.OSSUtils;
import cn.gdeiassistant.Tools.Utils.StringEncryptUtils;
import com.taobao.wsgsvr.WsgException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PhotographService {

    @Autowired
    private PhotographMapper photographMapper;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private OSSUtils ossUtils;

    /**
     * 查询照片统计数量
     *
     * @return
     */
    public int QueryPhotoStatisticalData() {
        return photographMapper.selectPhotographImageCount();
    }

    /**
     * 查询评论统计数量
     *
     * @return
     */
    public int QueryCommentStatisticalData() {
        return photographMapper.selectPhotographCommentCount();
    }

    /**
     * 查询点赞统计数量
     *
     * @return
     */
    public int QueryLikeStatisticalData() {
        return photographMapper.selectPhotographLikeCount();
    }

    /**
     * 分页查询照片信息列表
     *
     * @param start
     * @param size
     * @param type
     * @param sessionId
     * @return
     */
    public List<Photograph> QueryPhotographList(int start, int size, int type, String sessionId) throws WsgException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        List<Photograph> photographList = photographMapper.selectPhotograph(start, size, type
                , StringEncryptUtils.encryptString(user.getUsername()));
        //清除空行
        photographList.removeIf(photograph -> photograph.getId() == null);
        for (Photograph photograph : photographList) {
            photograph.setUsername(StringEncryptUtils.decryptString(photograph.getUsername()));
        }
        return photographList;
    }

    /**
     * 查询照片信息评论列表
     *
     * @param id
     * @return
     */
    public List<PhotographComment> QueryPhotographCommentList(int id) throws WsgException {
        List<PhotographComment> commentList = photographMapper.selectPhotographCommentByPhotoId(id);
        for (PhotographComment photographComment : commentList) {
            photographComment.setUsername(StringEncryptUtils.decryptString(photographComment.getUsername()));
        }
        return commentList;
    }

    /**
     * 添加照片信息
     *
     * @param title
     * @param content
     * @param count
     * @param type
     * @param sessionId
     */
    public int AddPhotograph(String title, String content, int count, int type, String sessionId) throws WsgException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Photograph photograph = new Photograph();
        photograph.setTitle(title);
        photograph.setContent(content);
        photograph.setCount(count);
        photograph.setType(type);
        photograph.setUsername(StringEncryptUtils.encryptString(user.getUsername()));
        photographMapper.insertPhotograph(photograph);
        return photograph.getId();
    }

    /**
     * 添加照片信息评论
     *
     * @param id
     * @param comment
     * @param sessionId
     * @throws WsgException
     */
    public void AddPhotographComment(int id, String comment, String sessionId) throws WsgException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        PhotographComment photographComment = new PhotographComment();
        photographComment.setPhotoId(id);
        photographComment.setComment(comment);
        photographComment.setUsername(StringEncryptUtils.encryptString(user.getUsername()));
        photographMapper.insertPhotographComment(photographComment);
    }

    /**
     * 上传照片信息图片
     *
     * @param id
     * @param index
     * @param inputStream
     * @return
     */
    public void UploadPhotographItemPicture(int id, int index, InputStream inputStream) {
        ossUtils.UploadOSSObject("gdeiassistant-userdata", "photograph/" + id + "_" + index + ".jpg", inputStream);
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取照片信息图片
     *
     * @param id
     * @param index
     * @return
     */
    public String GetPhotographItemPictureURL(int id, int index) {
        return ossUtils.GeneratePresignedUrl("gdeiassistant-userdata", "photograph/" + id + "_" + index + ".jpg"
                , 30, TimeUnit.MINUTES);
    }

    /**
     * 点赞照片信息
     *
     * @param id
     * @param sessionId
     */
    public void LikePhotograph(int id, String sessionId) throws WsgException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        int count = photographMapper.selectPhotographLikeCountByPhotoIdAndUsername(id, StringEncryptUtils.encryptString(user.getUsername()));
        if (count == 0) {
            photographMapper.insertPhotographLike(id, StringEncryptUtils.encryptString(user.getUsername()));
        }
    }

}
