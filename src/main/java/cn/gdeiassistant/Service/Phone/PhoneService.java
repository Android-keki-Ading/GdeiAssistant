package cn.gdeiassistant.Service.Phone;

import cn.gdeiassistant.Exception.PhoneException.SendSMSException;
import cn.gdeiassistant.Exception.PhoneException.VerificationCodeInvalidException;
import cn.gdeiassistant.Pojo.Entity.Phone;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Repository.Redis.VerificationCode.VerificationCodeDao;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Phone.PhoneMapper;
import cn.gdeiassistant.Service.CloudAPI.AliYunService;
import cn.gdeiassistant.Service.UserLogin.UserCertificateService;
import cn.gdeiassistant.Tools.Utils.StringEncryptUtils;
import com.aliyuncs.exceptions.ClientException;
import com.taobao.wsgsvr.WsgException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PhoneService {

    @Autowired
    private PhoneMapper phoneMapper;

    @Autowired
    private VerificationCodeDao verificationCodeDao;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private AliYunService aliYunService;

    /**
     * 查询用户绑定的手机号信息
     *
     * @param sessionId
     * @return
     */
    public Phone QueryUserPhone(String sessionId) throws WsgException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Phone phone = phoneMapper.selectPhone(StringEncryptUtils.encryptString(user.getUsername()));
        if (phone != null) {
            //解密手机号
            phone.setPhone(StringEncryptUtils.decryptString(phone.getPhone()));
            //隐藏用户绑定的手机号
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < phone.getPhone().length(); i++) {
                if (i < 3) {
                    stringBuilder.append(phone.getPhone().charAt(i));
                } else {
                    stringBuilder.append('*');
                }
            }
            phone.setPhone(stringBuilder.toString());
        }
        return phone;
    }

    /**
     * 获取手机验证码
     *
     * @param code
     * @param phone
     */
    public void GetPhoneVerificationCode(int code, String phone) throws ClientException, SendSMSException {
        //生成随机数
        int randomCode = (int) ((Math.random() * 9 + 1) * 100000);
        //写入Redis缓存记录
        verificationCodeDao.SavePhoneVerificationCode(code, phone, randomCode);
        if (code == 86) {
            //国内手机号
            aliYunService.SendChinaPhoneVerificationCodeSMS(randomCode, phone);
        } else {
            //国际/港澳台手机号
            aliYunService.SendGlobalPhoneVerificationCodeSMS(randomCode, code, phone);
        }
    }

    /**
     * 检测手机验证码正确性
     *
     * @param code
     * @param phone
     * @param randomCode
     */
    public void CheckVerificationCode(int code, String phone, int randomCode) throws VerificationCodeInvalidException {
        Integer verificationCode = verificationCodeDao.QueryPhoneVerificationCode(code, phone);
        if (verificationCode != null) {
            if (verificationCode.equals(randomCode)) {
                //移除手机验证码记录
                verificationCodeDao.DeletePhoneVerificationCode(code, phone);
                //校验通过
                return;
            }
        }
        throw new VerificationCodeInvalidException();
    }

    /**
     * 添加或更新绑定的手机号信息
     *
     * @param sessionId
     * @param code
     * @param phone
     */
    public void AttachUserPhone(String sessionId, Integer code, String phone) throws WsgException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Phone data = phoneMapper.selectPhone(StringEncryptUtils.encryptString(user.getUsername()));
        if (data != null) {
            data.setCode(code);
            data.setPhone(StringEncryptUtils.encryptString(phone));
            phoneMapper.updatePhone(data);
        } else {
            phoneMapper.insertPhone(StringEncryptUtils.encryptString(user.getUsername())
                    , code, StringEncryptUtils.encryptString(phone));
        }
    }

    /**
     * 解除绑定用户的手机号信息
     *
     * @param sessionId
     * @throws WsgException
     */
    public void UnAttachUserPhone(String sessionId) throws WsgException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Phone data = phoneMapper.selectPhone(StringEncryptUtils.encryptString(user.getUsername()));
        if (data != null) {
            phoneMapper.deletePhone(StringEncryptUtils.encryptString(user.getUsername()));
        }
    }
}
