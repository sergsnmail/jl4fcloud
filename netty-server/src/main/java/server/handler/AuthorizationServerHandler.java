package server.handler;

import db.DbStorage;
import db.user.User;
import db.user.UserDataSourceImpl;
import db.user.UserServiceImpl;
import message.*;
import message.method.auth.AuthParam;
import message.method.auth.AuthResult;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import message.method.reg.RegParam;
import message.method.reg.RegResult;

public class AuthorizationServerHandler extends ChannelInboundHandlerAdapter {

    private final String HANDLER_ID = "Authorization";
    private UserServiceImpl userService = new UserServiceImpl(new UserDataSourceImpl(new DbStorage()));

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(HANDLER_ID + " activate");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Request req = (Request)msg;
        System.out.println(HANDLER_ID + " handler " + req.toString());
        Method method = req.getMethod();
        if ("/auth".equals(method.getName())){
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
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause.getMessage());
        ctx.close();
    }
}
