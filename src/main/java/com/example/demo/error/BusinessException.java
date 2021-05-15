package com.example.demo.error;

public class BusinessException extends Exception implements CommonError{
    private CommonError commonError;

    //receive enum business err parameter, build business exception
    public BusinessException(CommonError commonError){
        super();
        this.commonError = commonError;
    }

    //receive
    public BusinessException(CommonError commonError, String errMsg){
        super();
        this.commonError = commonError;
        this.commonError.setErrMsg(errMsg);
    }


    @Override
    public int getErrCode() {
        return this.commonError.getErrCode();
    }

    @Override
    public String getErrMsg() {
        return this.commonError.getErrMsg();
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.commonError.setErrMsg(errMsg);
        return this;
    }
}
