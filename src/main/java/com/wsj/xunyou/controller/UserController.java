package com.wsj.xunyou.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wsj.xunyou.common.BaseResponse;
import com.wsj.xunyou.common.ErrorCode;
import com.wsj.xunyou.common.ResultUtils;
import com.wsj.xunyou.constant.UserConstant;
import com.wsj.xunyou.exception.BusinessException;
import com.wsj.xunyou.model.domain.User;
import com.wsj.xunyou.model.request.UserLoginRequest;
import com.wsj.xunyou.model.request.UserRegisterRequest;
import com.wsj.xunyou.service.UserService;
import com.wsj.xunyou.utils.DrawCheckCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.awt.image.BufferedImage;

import static com.wsj.xunyou.utils.MailUtils.sendMail;

/**
 * 用户接口
 *
 * @author yupi
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:3000"})
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest, HttpServletResponse response ,HttpServletRequest request) throws IOException {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String redisKey = "xunyou:user:confirmKey:";
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        String confirmKey = (String) valueOperations.get(redisKey);
        if (confirmKey == null || Integer.parseInt(userRegisterRequest.getCheckCode()) != Integer.parseInt(confirmKey)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误或已过期");
        }

        String userName = userRegisterRequest.getUserName();
        String email = userRegisterRequest.getEmail();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userName, email, userPassword, checkPassword)) {
            return null;
        }
        Long userid = userService.userRegister(userName, email, userPassword, checkPassword);

        // 注册完成后将生成的id作为登录验证码
        response.setHeader("Pragma","no-cache");
        response.setHeader("Cache-Control","no-cache");
        response.setHeader("Expires","0");
        //将验证码的值保存在session中，以便校验
        request.getSession().setAttribute("CHECKCODE",String.valueOf(userid));

        return ResultUtils.success(userid);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        if(!checkCode(userLoginRequest.getCheckCode(), request)){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR,"验证码错误");
        }
        String userEmail = userLoginRequest.getUserEmail();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userEmail, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userEmail, userPassword, request);
        if (user == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }
        return ResultUtils.success(user);
    }

    /**
     * 用于合法注册邮箱时、更改密码时使用
     */
    @GetMapping("/sentcheckcode")
    public void sentCheckCode(@RequestParam String mail, @RequestParam int opt) {
        // 在缓存中找验证码
        String redisKey = "xunyou:user:confirmKey:";
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        String confirmKey = (String) valueOperations.get(redisKey);
        if(confirmKey == null){
            confirmKey = "";
        }
        userService.getCheckCode(mail, redisKey, confirmKey, opt);
    }

    /**
     * 用于登录中即时检查验证码是否正确
     * @param codeClient
     * @param request
     * @return
     */
    @RequestMapping("/check")
    public boolean checkCode(@RequestParam String codeClient, HttpServletRequest request){
        String codeServer = (String)request.getSession().getAttribute("CHECKCODE");
        return codeClient.equals(codeServer);
    }

    /**
     * 用于登录中生成/刷新验证码
     * @param response
     * @param request
     * @throws IOException
     */
    @GetMapping("/checkcode")
    public void checkCodeMake(HttpServletResponse response, HttpServletRequest request) throws IOException {
        //画验证码
        DrawCheckCode drawCheckcode = new DrawCheckCode();
        BufferedImage image = drawCheckcode.doDraw();
        //设置响应头，防止缓存
        response.setHeader("Pragma","no-cache");
        response.setHeader("Cache-Control","no-cache");
        response.setHeader("Expires","0");
        //将验证码的值保存在session中，以便校验
        request.getSession().setAttribute("CHECKCODE",drawCheckcode.getCheckCode());
        ServletOutputStream outputStream = response.getOutputStream();
        ImageIO.write(image,"jpeg",outputStream);
        outputStream.flush();   //清空缓冲区数据
        outputStream.close();   //关闭流
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        // TODO 校验用户是否合法
        User user = userService.getById(userId);
//        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(user);
    }

    @GetMapping("/search/username")
    public BaseResponse<List<User>> searchUsersByUsername(@RequestParam(required = false) String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
//            queryWrapper.eq("id",userid);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @GetMapping("/search/userid")
    public BaseResponse<List<User>> searchUsersById(@RequestParam(required = false) String userid) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(userid)) {
//            queryWrapper.like("username", username);
            queryWrapper.eq("id", userid);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @GetMapping("/searchMutiId")
    public BaseResponse<List<User>> searchMutiUsers(@RequestParam List<Integer> usersid) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", usersid);
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }


    // todo 推荐多个，未实现
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("xunyou:user:recommend:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 如果有缓存，直接读缓存
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            return ResultUtils.success(userPage);
        }
        // 无缓存，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        // 写缓存
        try {
            // 指定缓存30秒过期时间
            valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return ResultUtils.success(userPage);
    }

    @GetMapping("/totalItems")
    public BaseResponse<Integer> totalItems() {
        int result = (int) userService.getTotalItems();
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        // BaseResponse<Integer> 泛型要用包装类，不能用int
        // 1.校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2.校验权限（鉴权）
        User loginUser = userService.getLoginUser(request);
        // 3.触发更新
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }
    @RequestMapping("/resetPwtCheckCode")
    public boolean forgetPwtCheckCode(@RequestParam String resetPwdCheckCode) {
        String redisKey = "xunyou:user:confirmKey:";
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        String confirmKey = (String) valueOperations.get(redisKey);
        if (confirmKey == null || Integer.parseInt(resetPwdCheckCode) != Integer.parseInt(confirmKey)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误或已过期");
        }else{
            return true;
        }
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 获取最匹配的用户
     *
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(5, user));
    }

}
