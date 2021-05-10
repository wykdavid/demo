package com.example.demo.controller;

import com.alibaba.druid.util.StringUtils;
import com.example.demo.controller.viewobject.ItemVO;
import com.example.demo.controller.viewobject.UserVO;
import com.example.demo.error.BusinessException;
import com.example.demo.error.EnumBUsinessError;
import com.example.demo.responce.CommonReturnType;
import com.example.demo.service.UserService;
import com.example.demo.service.model.ItemModel;
import com.example.demo.service.model.UserModel;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class UserController extends BaseController {
    @Autowired
    private UserService userService;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private RedisTemplate redisTemplate;

    public UserController() {
    }
    @RequestMapping(
            value = {"/list"},
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public CommonReturnType listItem() throws BusinessException {
        String token = ((String[])this.httpServletRequest.getParameterMap().get("token"))[0];
        if (org.apache.commons.lang3.StringUtils.isEmpty(token)) {
            throw new BusinessException(EnumBUsinessError.USER_NOT_LOGIN, "User has not log in");
        } else {
            UserModel userModel = (UserModel)this.redisTemplate.opsForValue().get(token);
            if (userModel == null) {
                throw new BusinessException(EnumBUsinessError.USER_NOT_LOGIN, "session expire");
            } else {
                List<ItemModel> itemModelList = this.userService.listMyItem(userModel.getId());
                List<ItemVO> itemVOList = (List)itemModelList.stream().map((itemModel) -> {
                    ItemVO itemVO = this.convertVOFromModel(itemModel);
                    return itemVO;
                }).collect(Collectors.toList());
                return CommonReturnType.create(itemVOList);
            }
        }

    }
    @RequestMapping(
            value = {"/MyOrder"},
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public CommonReturnType listMyOrder() throws BusinessException {
        String token = ((String[])this.httpServletRequest.getParameterMap().get("token"))[0];
        if (org.apache.commons.lang3.StringUtils.isEmpty(token)) {
            throw new BusinessException(EnumBUsinessError.USER_NOT_LOGIN, "User has not log in");
        } else {
            UserModel userModel = (UserModel)this.redisTemplate.opsForValue().get(token);
            if (userModel == null) {
                throw new BusinessException(EnumBUsinessError.USER_NOT_LOGIN, "session expire");
            } else {
                List<ItemModel> itemModelList = this.userService.listMyOrder(userModel.getId());
                List<ItemVO> itemVOList = (List)itemModelList.stream().map((itemModel) -> {
                    ItemVO itemVO = this.convertVOFromModel(itemModel);
                    return itemVO;
                }).collect(Collectors.toList());
                return CommonReturnType.create(itemVOList);
            }
        }

    }
    private ItemVO convertVOFromModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        } else {
            ItemVO itemVO = new ItemVO();
            BeanUtils.copyProperties(itemModel, itemVO);
            if (itemModel.getPromoModel() != null) {
                itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
                itemVO.setPromoId(itemModel.getPromoModel().getId());
                itemVO.setStartDate(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
                itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
            } else {
                itemVO.setPromoStatus(0);
            }

            return itemVO;
        }
    }

    @RequestMapping(
            value = {"/login"},
            method = {RequestMethod.POST},
            consumes = {"application/x-www-form-urlencoded"}
    )
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telephone") String telephone, @RequestParam(name = "password") String password) throws BusinessException, EncoderException, NoSuchAlgorithmException, UnsupportedEncodingException {
        if (!StringUtils.isEmpty(telephone) && !StringUtils.isEmpty(password)) {
            UserModel userModel = this.userService.validateLogin(telephone, this.EncodeByMd5(password));
            String uuidToken = UUID.randomUUID().toString();
            uuidToken = uuidToken.replace("-", "");
            this.redisTemplate.opsForValue().set(uuidToken, userModel);
            this.redisTemplate.expire(uuidToken, 1L, TimeUnit.HOURS);
            return CommonReturnType.create(uuidToken);
        } else {
            throw new BusinessException(EnumBUsinessError.PARAMETER_VALIDATION_ERROR);
        }
    }

    @RequestMapping(
            value = {"/register"},
            method = {RequestMethod.POST},
            consumes = {"application/x-www-form-urlencoded"}
    )
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telephone") String telephone, @RequestParam(name = "otpCode") String otpCode, @RequestParam(name = "name") String name, @RequestParam(name = "gender") Integer gender, @RequestParam(name = "age") Integer age, @RequestParam(name = "password") String password) throws BusinessException, EncoderException, NoSuchAlgorithmException, UnsupportedEncodingException {
        String inSessionOtpCode = (String)this.httpServletRequest.getSession().getAttribute(telephone);
        if (!StringUtils.equals(otpCode, inSessionOtpCode)) {
            throw new BusinessException(EnumBUsinessError.PARAMETER_VALIDATION_ERROR, "otp code wrong");
        }
        else if(telephone.indexOf("@") == -1 || telephone.split("@").length>2 || !telephone.split("@")[1].equals("syr.edu")){
            throw new BusinessException(EnumBUsinessError.WRONG_ACCOUNT);
        } else {
            UserModel userModel = new UserModel();
            userModel.setName(name);
            userModel.setGender(new Byte(String.valueOf(gender)));
            userModel.setAge(age);
            userModel.setTelephone(telephone);
            userModel.setRegisterMode("byEmail");
            userModel.setEncryptPassword(this.EncodeByMd5(password));
            this.userService.register(userModel);
            return CommonReturnType.create((Object)null);
        }
    }

    public String EncodeByMd5(String str) throws EncoderException, UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        String newstr = Base64.encodeBase64String(md.digest(str.getBytes("utf-8")));
        return newstr;
    }

    @RequestMapping(
            value = {"/getotp"},
            method = {RequestMethod.POST},
            consumes = {"application/x-www-form-urlencoded"}
    )
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telephone") String telephone) throws BusinessException {
        if(telephone.indexOf("@") == -1 || telephone.split("@").length>2 || !telephone.split("@")[1].equals("syr.edu")){
            throw new BusinessException(EnumBUsinessError.WRONG_ACCOUNT);
        }
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);
        this.httpServletRequest.getSession().setAttribute(telephone, otpCode);
        System.out.println("email = " + telephone + "&otpCode = " + otpCode);
        return CommonReturnType.create((Object)null);
    }

    @RequestMapping({"/get"})
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BusinessException {
        UserModel userModel = this.userService.getUserById(id);
        if (userModel == null) {
            throw new BusinessException(EnumBUsinessError.USER_NOT_EXIST);
        } else {
            UserVO userVO = this.convertFromModel(userModel);
            return CommonReturnType.create(userVO);
        }
    }

    private UserVO convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        } else {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(userModel, userVO);
            return userVO;
        }
    }
}
