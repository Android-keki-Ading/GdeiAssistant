package com.gdeiassistant.gdeiassistant.Service.Privacy;

import com.gdeiassistant.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.gdeiassistant.gdeiassistant.Repository.Mongodb.Grade.GradeDao;
import com.gdeiassistant.gdeiassistant.Repository.Mongodb.Schedule.ScheduleDao;
import com.gdeiassistant.gdeiassistant.Repository.Mysql.GdeiAssistant.Privacy.PrivacyMapper;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.Privacy;
import com.gdeiassistant.gdeiassistant.Pojo.Result.BaseResult;
import com.gdeiassistant.gdeiassistant.Tools.StringEncryptUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class PrivacyService {

    @Resource(name = "privacyMapper")
    private PrivacyMapper privacyMapper;

    @Autowired
    private GradeDao gradeDao;

    @Autowired
    private ScheduleDao scheduleDao;

    private Log log = LogFactory.getLog(PrivacyService.class);

    /**
     * 获取用户隐私设置
     *
     * @param username
     * @return
     */
    public BaseResult<Privacy, DataBaseResultEnum> GetPrivacySetting(String username) {
        BaseResult<Privacy, DataBaseResultEnum> result = new BaseResult<>();
        try {
            Privacy privacy = privacyMapper.selectPrivacy(StringEncryptUtils.encryptString(username));
            if (privacy == null) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                result.setResultData(privacy);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("获取用户隐私配置异常：" , e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 更新性别隐私设置
     *
     * @param state
     * @param username
     * @return
     */
    public boolean UpdateGender(boolean state, String username) {
        try {
            privacyMapper.updateGender(state, StringEncryptUtils.encryptString(username));
            return true;
        } catch (Exception e) {
            log.error("更新性别隐私配置异常：" , e);
            return false;
        }
    }

    /**
     * 更新性取向隐私设置
     *
     * @param state
     * @param username
     * @return
     */
    public boolean UpdateGenderOrientation(boolean state, String username) {
        try {
            privacyMapper.updateGenderOrientation(state, StringEncryptUtils.encryptString(username));
            return true;
        } catch (Exception e) {
            log.error("更新性取向隐私配置异常：" , e);
            return false;
        }
    }

    /**
     * 更新国家/地区隐私设置
     *
     * @param state
     * @param username
     * @return
     */
    public boolean UpdateLocation(boolean state, String username) {
        try {
            privacyMapper.updateRegion(state, StringEncryptUtils.encryptString(username));
            return true;
        } catch (Exception e) {
            log.error("更新国家和地区隐私配置异常：" , e);
            return false;
        }
    }

    /**
     * 更新个人简介隐私设置
     *
     * @param state
     * @param username
     * @return
     */
    public boolean UpdateIntroduction(boolean state, String username) {
        try {
            privacyMapper.updateIntroduction(state, StringEncryptUtils.encryptString(username));
            return true;
        } catch (Exception e) {
            log.error("更新个人简介隐私配置异常：" , e);
            return false;
        }
    }

    /**
     * 更新教务信息缓存隐私配置
     *
     * @param state
     * @param username
     * @return
     */
    public boolean UpdateCache(boolean state, String username) {
        try {
            privacyMapper.updateCache(state, StringEncryptUtils.encryptString(username));
            gradeDao.removeGrade(username);
            scheduleDao.removeSchedule(username);
            return true;
        } catch (Exception e) {
            log.error("更新教务信息缓存隐私配置异常：" , e);
            return false;
        }
    }
}
