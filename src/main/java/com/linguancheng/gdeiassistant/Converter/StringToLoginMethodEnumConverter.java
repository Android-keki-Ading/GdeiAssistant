package com.gdeiassistant.gdeiassistant.Converter;

import org.springframework.core.convert.converter.Converter;
import com.gdeiassistant.gdeiassistant.Enum.Base.LoginMethodEnum;

/**
 * ��String�ַ�������ת��ΪLoginMethodEnumö������
 */
public class StringToLoginMethodEnumConverter implements Converter<String, LoginMethodEnum> {

    @Override
    public LoginMethodEnum convert(String source) {
        return LoginMethodEnum.getEnumByValue(source);
    }
}
