package com.example.group11.controller.user;


import com.example.group11.commons.utils.*;
import com.example.group11.model.UserModel;
import com.example.group11.service.user.UserService;
import com.example.group11.vo.RespondentVO;
import com.example.group11.vo.UserToRespondentVO;
import com.example.group11.vo.UserVO;
import com.example.group11.vo.query.UserQueryVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@Slf4j
public class UserController {

    @DubboReference(version = "1.0.0", interfaceClass = com.example.group11.service.user.UserService.class)
    private UserService userService;

    @PostMapping("/sys/user/login")
    @ApiOperation(notes = "用户登录获取token", value = "用户登录获取token", tags = "用户管理")
    public RestResult login(@RequestParam("loginName") String loginName,
                            @RequestParam("password") String password) {
        UserModel userModel = userService.queryUserByLoginName(loginName);
        if (userModel.getPassword().equals(ShiroUtil.sha256(password, userModel.getSalt()))) {
            return RestResult.ok(JWTUtil.sign(userModel.getLoginName(), userModel.getId()));
        } else {
            throw Group11Exception.unauthorized("未认证");
        }
    }

    @GetMapping("/sys/user/loginname/{loginName}")
    @ApiOperation(notes = "根据用户名查询用户信息", value = "根据用户名查询用户信息", tags = "用户管理")
    public RestResult<UserVO> queryUserByLoginName(@PathVariable String loginName) {
        UserModel userModel = userService.queryUserByLoginName(loginName);
        return RestResult.ok(OrikaUtil.map(userModel, UserVO.class));
    }

    @GetMapping("/sys/user/id/{id}")
    @ApiOperation(notes = "根据用户id查询用户信息", value = "根据用户id查询用户信息", tags = "用户管理")
    public RestResult<UserVO> queryById(@PathVariable Long id) {
        UserModel userModel = userService.findById(id);
        return RestResult.ok(OrikaUtil.map(userModel, UserVO.class));
    }

    @GetMapping("/sys/user/me")
    @ApiOperation(notes = "查看当前登录用户信息", value = "查看当前登录用户信息", tags = "用户管理")
    public RestResult<UserVO> queryCurrentUser(HttpServletRequest httpServletRequest) {
        return RestResult.ok();
    }

    @PostMapping("/sys/user/me/audio")
    @ApiOperation(notes = "上传音频自我介绍文件返回语音自我介绍url", value = "上传音频", tags = "用户管理")
    public RestResult<String> uploadAudioFile(@RequestParam MultipartFile file, HttpServletRequest httpServletRequest) {
        return RestResult.ok();
    }

    @PostMapping("/sys/user/me/respondent")
    @ApiOperation(notes = "成为答主,调用该接口前，首先调用上传语音自我介绍文件接口得到返回的语音自我介绍url", value = "成为答主", tags = "用户管理")
//调用该接口前，首先调用上传语音自我介绍文件得到返回的语音自我介绍url
    public RestResult<RespondentVO> toRespondent(@RequestBody UserToRespondentVO userToRespondentVO,
                                                 HttpServletRequest httpServletRequest) {
        return RestResult.ok();
    }

    @PostMapping("/exapi/sys/user/id")
    @ApiOperation(notes = "新增用户信息", value = "新增用户信息", tags = "用户管理")
    public RestResult<UserVO> updateUserById(@RequestBody UserModel form) {
        log.info("[insertUser],form={}", form);
        String salt = RandomStringUtils.randomAlphanumeric(20);
        form.setSalt(salt);
        form.setPassword(ShiroUtil.sha256(form.getPassword(), salt));
        Long id = userService.insertOne(form);
        UserModel userModel = userService.findById(id);
        return RestResult.ok(OrikaUtil.map(userModel, UserVO.class));
    }

    @PutMapping("/sys/user/id/{id}")
    @ApiOperation(notes = "修改用户信息", value = "修改用户信息", tags = "用户管理")
    public RestResult<UserVO> updateUserById(@PathVariable Long id, @RequestBody UserModel form,
                                             HttpServletRequest httpServletRequest) {
        String token = JWTUtil.getToken(httpServletRequest);
        Long userId = JWTUtil.getUserId(token);
        log.info("[insertUser],form={},userId={}", form, userId);
        form.setId(id);
//        form.setUpdateBy(userId.toString());
        //用id更新用户数据
        userService.updateById(form);
        UserModel userModel = userService.findById(id);
        return RestResult.ok(OrikaUtil.map(userModel, UserVO.class));
    }

    @GetMapping("/sys/user")
    @ApiOperation(notes = "根据多条件分页查询用户列表", value = "根据多条件分页查询用户列表", tags = "用户管理")
    public RestResult<Page<UserVO>> queryUserListByPage(UserQueryVO params) {
        log.info("[queryUserList] params={}", params);
        Page<UserModel> modelPage = userService.queryUserListByPage(params);
        List<UserVO> userVOList = OrikaUtil.mapAsList(modelPage.getContent(), UserVO.class);
        return RestResult.ok(new PageImpl<>(userVOList, modelPage.getPageable(), modelPage.getTotalElements()));
    }


}
