package com.sergsnmail.server.handler;

import com.sergsnmail.common.message.Request;
import com.sergsnmail.common.message.Response;
import com.sergsnmail.server.db.DbStorage;
import com.sergsnmail.server.db.user.User;
import com.sergsnmail.server.db.user.UserDataSourceImpl;
import com.sergsnmail.server.db.user.UserServiceImpl;
import com.sergsnmail.common.message.common.Method;
import com.sergsnmail.common.message.method.auth.AuthMethod;
import com.sergsnmail.common.message.method.auth.AuthParam;
import com.sergsnmail.common.message.method.auth.AuthResult;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import com.sergsnmail.common.message.common.UserSession;
import com.sergsnmail.common.message.method.registration.RegParam;
import com.sergsnmail.common.message.method.registration.RegResult;
import com.sergsnmail.common.message.method.registration.RegMethod;

public class AuthorizationServerHandler extends ChannelInboundHandlerAdapter {

    private final String HANDLER_ID = "Authorization";
    private final UserSession userSession;
    private UserServiceImpl userService = new UserServiceImpl(new UserDataSourceImpl(new DbStorage()));

    public AuthorizationServerHandler(UserSession userSession) {
        this.userSession = userSession;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(HANDLER_ID + " activate");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Request req = (Request)msg;
        System.out.println(HANDLER_ID + " handler " + req.toString());
        Method method = req.getMethod();

        if (method instanceof AuthMethod){
            AuthParam authParam = ((AuthMethod) method).getParameter();
            if (authParam != null){
                String username = authParam.getUsername();
                String pass = authParam.getPassword();

                AuthResult authResult = new AuthResult();
                method.setResult(authResult);
                Response resp = Response.builder()
                        .setMethod(method)
                        .build();

                if (userService.authorizedUser(username, pass)){ // is authorized
                    authResult.setAuth(true);
                    userSession.setUsername(username);
                    authResult.setSession(userSession);

                    ctx.writeAndFlush(resp);
                    ctx.channel().pipeline().remove(this);
                } else {
                    authResult.setAuth(false);
                    authResult.setMessage("User not authorized");
                    ctx.writeAndFlush(resp);
                }
            }
        } else if (method instanceof RegMethod){
            RegParam regParam = ((RegMethod) method).getParameter();
            if (regParam != null){
                RegResult regResult = new RegResult();
                Response resp = Response.builder()
                        .setMethod(method)
                        .build();
                User newUser = userService.registerUser(regParam.getUsername(),"example@example.com",regParam.getPassword());
                if (newUser != null) {
                    regResult.setAuth(true);
                    userSession.setUserid(newUser.getUserId());
                    userSession.setUsername(newUser.getUsername());
                    regResult.setSession(userSession);
                    ctx.writeAndFlush(resp);
                    ctx.channel().pipeline().remove(this);
                } else {
                    regResult.setAuth(false);
                    regResult.setMessage("Error while registered new User");
                    ctx.writeAndFlush(resp);
                }
            }
        } else {
            throw new IllegalArgumentException("Unknown request");
        }
        /*if ("/auth".equals(method.getName())){
            AuthParam authParam = method.getParamImpl(AuthParam.class);
            if (authParam != null){
                String username = authParam.getUsername();
                String pass = authParam.getPassword();

                AuthResult authResult = new AuthResult();
                Response resp = Response.builder()
                        .setMethod(method)
                        .setMethodResult(authResult)
                        .build();

                if (userService.authorizedUser(username, pass)){ // is authorized
                    authResult.setAuth(true);

                    UserSession session = new UserSession(username);

                    ctx.writeAndFlush(resp);
                    ctx.channel().pipeline().remove(this);
                } else {
                    authResult.setAuth(false);
                    authResult.setMessage("User not authorized");
                    ctx.writeAndFlush(resp);
                }
            }
        } else if ("/register".equals(method.getName())) {
            RegParam regParam = method.getParamImpl(RegParam.class);
            if (regParam == null){
                throw new NullPointerException("param must be non-null but is null");
            }

            User newUser = userService.registerUser(regParam.getUsername(),"example@example.com",regParam.getPassword());
            RegResult regResult = new RegResult();
            Response resp = Response.builder()
                    .setMethod(method)
                    .setMethodResult(regResult)
                    .build();
            if (newUser == null) {
                regResult.setAuth(true);
                ctx.writeAndFlush(resp);
                ctx.channel().pipeline().remove(this);
            } else {
                regResult.setAuth(false);
                regResult.setMessage("Error while registered new User");
                ctx.writeAndFlush(resp);
            }
        } else {
            throw new IllegalArgumentException("Unknown auth request");
        }*/
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause.getMessage());
        ctx.close();
    }
}