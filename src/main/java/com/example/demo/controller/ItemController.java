package com.example.demo.controller;

import com.example.demo.controller.viewobject.ItemVO;
import com.example.demo.error.BusinessException;
import com.example.demo.error.EnumBUsinessError;
import com.example.demo.responce.CommonReturnType;
import com.example.demo.service.CacheServiceImpl;
import com.example.demo.service.ItemService;
import com.example.demo.service.PromoService;
import com.example.demo.service.UserService;
import com.example.demo.service.model.ItemModel;
import com.example.demo.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller("item")
@RequestMapping("/item")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class ItemController extends BaseController {
    @Autowired
    private ItemService itemService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CacheServiceImpl cacheService;
    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    public ItemController() {
    }

    @RequestMapping(
            value = {"/create"},
            method = {RequestMethod.POST},
            consumes = {"application/x-www-form-urlencoded"}
    )
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title") String title, @RequestParam(name = "description") String description, @RequestParam(name = "price") BigDecimal price, @RequestParam(name = "stock") Integer stock, @RequestParam(name = "imgUrl") String imgUrl) throws BusinessException {
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setStock(stock);
        itemModel.setPrice(price);
        itemModel.setImgUrl(imgUrl);

        String token = ((String[])this.httpServletRequest.getParameterMap().get("token"))[0];
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(EnumBUsinessError.USER_NOT_LOGIN, "User has not log in");
        } else {
            UserModel userModel = (UserModel)this.redisTemplate.opsForValue().get(token);
            if (userModel == null) {
                throw new BusinessException(EnumBUsinessError.USER_NOT_LOGIN, "session expire");
            } else {
                ItemModel itemModel1ForReturn = this.itemService.createItem(itemModel,userModel);
                //return CommonReturnType.create((Object)null);
                ItemVO itemVO = this.convertVOFromModel(itemModel1ForReturn);
                return CommonReturnType.create(itemVO);
            }
        }

    }

    @RequestMapping(
            value = {"/get"},
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id") Integer id) {
        ItemModel itemModel = null;
        itemModel = (ItemModel)this.cacheService.getFromCommonCache("item_" + id);
        if (itemModel == null) {
            itemModel = (ItemModel)this.redisTemplate.opsForValue().get("item_" + id);
            if (itemModel == null) {
                itemModel = this.itemService.getItemById(id);
                this.redisTemplate.opsForValue().set("item_" + id, itemModel);
                this.redisTemplate.expire("item_" + id, 10L, TimeUnit.MINUTES);
            }

            this.cacheService.setCommonCache("item_" + id, itemModel);
        }

        ItemVO itemVO = this.convertVOFromModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

    @RequestMapping(
            value = {"/list"},
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public CommonReturnType listItem() {
        List<ItemModel> itemModelList = this.itemService.listItem();
        List<ItemVO> itemVOList = (List)itemModelList.stream().map((itemModel) -> {
            ItemVO itemVO = this.convertVOFromModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());
        return CommonReturnType.create(itemVOList);
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
}
