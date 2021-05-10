package com.example.demo.error;

public enum EnumBUsinessError implements CommonError {
    //common error type 00001
    PARAMETER_VALIDATION_ERROR(10001,"parameter invalid"),
    UNKNOWN_ERROR(10002,"Unknown Error"),
    WRONG_ACCOUNT(10003,"This account is not valid, please use syr email"),

    //10000 user inf err
    USER_NOT_EXIST(20001,"User Not Exist"),
    USER_LOGIN_FAIL(20002,"telephone or password not correct"),
    USER_NOT_LOGIN(20003,"User has not log in"),
    STOCK_NOT_ENOUGH(300001,"Stock not enough"),
    MQ_SEND_FAIL(300002,"库存异步失败")
    ;

    private EnumBUsinessError(int errCode, String errMsg){
        this.errCode=errCode;
        this.errMsg=errMsg;
    }
    private int errCode;
    private String errMsg;
    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
