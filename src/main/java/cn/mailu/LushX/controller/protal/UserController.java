package cn.mailu.LushX.controller.protal;

import cn.mailu.LushX.common.ServerResponse;
import cn.mailu.LushX.constant.RedisKey;
import cn.mailu.LushX.constant.VideoTypeEnum;
import cn.mailu.LushX.entity.Article;
import cn.mailu.LushX.entity.User;
import cn.mailu.LushX.entity.Video;
import cn.mailu.LushX.security.JWTUserDetails;
import cn.mailu.LushX.security.JWTUserFactory;
import cn.mailu.LushX.service.FileService;
import cn.mailu.LushX.service.RedisService;
import cn.mailu.LushX.service.UserService;
import cn.mailu.LushX.util.CommonUtils;
import cn.mailu.LushX.util.JWTUtils;
import cn.mailu.LushX.util.MD5Utils;
import cn.mailu.LushX.vo.UserVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import io.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Author: NULL
 * @Description:
 * @Date: Create in 2017/11/5 19:57
 */
@RestController
@Api(value = "UserController", description = "用户相关接口")
public class UserController {

    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;


    @Autowired
    private FileService fileService;

    @Value("${jwt.header}")
    private String token_header;

    @Resource
    private JWTUtils jwtUtils;

    @ApiOperation(value = "注册用户", notes = "根据User对象创建用户")
    @ApiImplicitParam(name = "user", value = "只需要username和password字段", required = true, dataType = "User")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ServerResponse<String> register(@RequestBody User user) {
        logger.info(user.getGender());
        logger.info(user.getUsername());
        return userService.register(user);
    }

    @ApiOperation(value = "用户登录")
    @ApiImplicitParam(name = "user", value = "只需要username和password字段", required = true, dataType = "User")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ServerResponse login(@RequestBody User user) {
        User userNew = null;
        logger.info("username:{}", user.getUsername());
        logger.info("password:{}", user.getPassword());
        logger.info("password:{}", MD5Utils.MD5EncodeUtf8(user.getPassword()));
        userNew = userService.findByUsernameAndPassword(user.getUsername(), MD5Utils.MD5EncodeUtf8(user.getPassword()));
        if (userNew != null) {
            String token = null;
            try {
                token = jwtUtils.generateAccessToken(JWTUserFactory.create(userNew));
                Map<String, Object> map = Maps.newHashMap();
                map.put(token_header, "Bearer " + token);
                //todo 删除
                UserVO userVo=toUserVO(userNew);
                List<Video> videos = (List<Video>) redisService.getValueByKey(RedisKey.VIDEOS_KEY + "_" + VideoTypeEnum.CL_TV_HOT.getCode());
                Pageable pageable = new PageRequest(0, 10);
                Page<Video> videoPage = CommonUtils.getPage(pageable, videos);
                List<Article> articles = (List<Article>) redisService.getValueByKey(RedisKey.JIANSHU_TRENDING_KEY + "_" + RedisKey.TAGS[2]);
                Page<Article> articlePage = CommonUtils.getPage(pageable, articles);
                Map map2 = Maps.newHashMap();
                map2.put("video", videoPage);
                map2.put("article", articlePage);
                userVo.setCollection(map2);
                map.put("info", userVo);
                logger.info("验证成功，发出token");
                return ServerResponse.createBySuccess(map);
            } catch (JsonProcessingException e) {
                logger.error("generateAccessToken error");
            }
        }
        return ServerResponse.createByErrorMessage("用户名或密码错误");
    }

    @ApiOperation(value = "用户首页", notes = "用户首页")
    @GetMapping("/u")
    public ServerResponse userspace(@AuthenticationPrincipal JWTUserDetails jwtuser) {
        if (jwtuser != null) {
            User user = userService.selectById(jwtuser.getUserId());
            UserVO userVo = toUserVO(user);
            // todo 获取收藏列表
            List<Video> videos = (List<Video>) redisService.getValueByKey(RedisKey.VIDEOS_KEY + "_" + VideoTypeEnum.CL_TV_HOT.getCode());
            Pageable pageable = new PageRequest(0, 10);
            Page<Video> videoPage = CommonUtils.getPage(pageable, videos);
            List<Article> articles = (List<Article>) redisService.getValueByKey(RedisKey.JIANSHU_TRENDING_KEY + "_" + RedisKey.TAGS[2]);
            Page<Article> articlePage = CommonUtils.getPage(pageable, articles);
            Map map = Maps.newHashMap();
            map.put("video", videoPage);
            map.put("article", articlePage);
            userVo.setCollection(map);
            return ServerResponse.createBySuccess(userVo);
        }
        return ServerResponse.createByErrorMessage("未登录");
    }


    @ApiOperation(value = "更新用户头像", notes = "更新用户头像")
    @ApiImplicitParam(name = "headImg", value = "headImg传base64字符串", required = true, dataType = "String")
    @PostMapping("/u/avatar")
    public ServerResponse updateAvatar(@AuthenticationPrincipal JWTUserDetails jwtuser, @ApiParam(hidden = true) @RequestBody User user) {
        if (jwtuser != null) {
            Map map = fileService.uploadImage(user.getHeadImg());
            if (((int) map.get("status") == 0)) {
                String headImg = (String) map.get("message");
                logger.info("图片地址{}", headImg);
                user.setUserId(jwtuser.getUserId());
                user.setHeadImg(headImg);
                if (userService.updateSelective(user) == null) {
                    return ServerResponse.createByErrorMessage("更新用户头像错误");
                }
                return ServerResponse.createBySuccess(headImg);
            }
            return ServerResponse.createByErrorMessage("图片上传错误");
        }
        return ServerResponse.createByErrorMessage("未登录");
    }

    @ApiOperation(value = "更新用户信息", notes = "更新用户信息")
    @PostMapping("/u")
    public ServerResponse updateUser(@AuthenticationPrincipal @ApiParam(hidden = true) JWTUserDetails jwtuser, @ApiParam(required = true) @RequestBody User user) {
        if (jwtuser != null) {
            user.setUserId(jwtuser.getUserId());
            if (StringUtils.isNotEmpty(user.getPassword())) {
                user.setPassword(MD5Utils.MD5EncodeUtf8(user.getPassword()));
            }
            if (StringUtils.isNotEmpty(user.getUsername())) {
                User res=userService.findByUsername(user.getUsername());
                if(res!=null){
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
                user.setMd5(MD5Utils.MD5EncodeUtf8(user.getUsername()));
            }
            User userNew = userService.updateSelective(user);
            if (userNew != null) {
                String token = null;
                try {
                    token = jwtUtils.generateAccessToken(JWTUserFactory.create(userNew));
                    Map<String, Object> map = Maps.newHashMap();
                    map.put(token_header, "Bearer " + token);
                    //todo 删除
                    UserVO userVo=toUserVO(userNew);
                    List<Video> videos = (List<Video>) redisService.getValueByKey(RedisKey.VIDEOS_KEY + "_" + VideoTypeEnum.CL_TV_HOT.getCode());
                    Pageable pageable = new PageRequest(0, 10);
                    Page<Video> videoPage = CommonUtils.getPage(pageable, videos);
                    List<Article> articles = (List<Article>) redisService.getValueByKey(RedisKey.JIANSHU_TRENDING_KEY + "_" + RedisKey.TAGS[2]);
                    Page<Article> articlePage = CommonUtils.getPage(pageable, articles);
                    Map map2 = Maps.newHashMap();
                    map2.put("video", videoPage);
                    map2.put("article", articlePage);
                    userVo.setCollection(map2);
                    map.put("info", userVo);
                    logger.info("修改用户信息，验证成功，发出token");
                    return ServerResponse.createBySuccess(map);
                } catch (JsonProcessingException e) {
                    logger.error("generateAccessToken error");
                }
            }
            return ServerResponse.createByErrorMessage("更新信息失败");
        }
        return ServerResponse.createByErrorMessage("未登录");
    }

    @PostMapping("/u/dislike")
    @ApiOperation(value = "取消收藏")
    @ApiImplicitParam(name = "id", value = "取消收藏的id", required = true, paramType = "query")
    public ServerResponse dislike(@AuthenticationPrincipal @ApiParam(hidden = true) JWTUserDetails jwtuser, @RequestBody String id) {
        //todo 取消收藏
        return ServerResponse.createByError();
    }

    //生成UserVO
    private UserVO toUserVO(User user) {
        UserVO userVO = new UserVO();
        userVO.setUserId(user.getUserId());
        userVO.setHeadImg(user.getHeadImg());
        userVO.setUsername(user.getUsername());
        return userVO;
    }


}
