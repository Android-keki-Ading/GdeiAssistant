package com.gdeiassistant.gdeiassistant.Service.YiBan;

import com.gdeiassistant.gdeiassistant.Enum.Base.AttachResultEnum;
import com.gdeiassistant.gdeiassistant.Repository.Mysql.GdeiAssistant.YiBanUser.YiBanUserMapper;
import com.gdeiassistant.gdeiassistant.Pojo.Result.BaseResult;
import com.gdeiassistant.gdeiassistant.Tools.StringEncryptUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class YiBanUserDataService {

    @Resource(name = "yiBanUserMapper")
    private YiBanUserMapper yiBanUserMapper;

    private Log log = LogFactory.getLog(YiBanUserDataService.class);

    /**
     * 同步易班账号与教务系统账号绑定的数据
     *
     * @param username
     * @param userID
     * @return
     */
    public boolean SyncYiBanUserData(String username, String userID) {
        try {
            int id = Integer.valueOf(userID);
            if (yiBanUserMapper.selectUsername(id) != null) {
                //更新易班账号绑定的教务系统账号
                yiBanUserMapper.updateYiBanUser(id, StringEncryptUtils.encryptString(username));
            } else {
                yiBanUserMapper.insertYiBanUser(id, StringEncryptUtils.encryptString(username));
            }
            return true;
        } catch (Exception e) {
            log.error("同步易班绑定账号异常：" , e);
            return false;
        }
    }

    /**
     * 检查易班绑定教务系统的状态
     *
     * @param userID
     * @return
     */
    public BaseResult<String, AttachResultEnum> CheckYiBanAttachState(String userID) {
        BaseResult<String, AttachResultEnum> result = new BaseResult<>();
        try {
            int id = Integer.valueOf(userID);
            String encryptedUsername = yiBanUserMapper.selectUsername(id);
            if (encryptedUsername == null || encryptedUsername.trim().isEmpty()) {
                //账号未绑定教务系统
                result.setResultType(AttachResultEnum.NOT_ATTACHED);
            } else {
                //账号已绑定教务系统
                result.setResultData(StringEncryptUtils.decryptString(encryptedUsername));
                result.setResultType(AttachResultEnum.ATTACHED);
            }
        } catch (Exception e) {
            log.error("检查易班绑定账号状态异常：" , e);
            result.setResultType(AttachResultEnum.SERVER_ERROR);
        }
        return result;
    }
}
