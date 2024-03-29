package net.xdclass.order_service.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import net.xdclass.order_service.service.ProductOrderService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("api/v1/order")
public class OrderController {

    @Autowired
    private ProductOrderService productOrderService;


    @Autowired
    private StringRedisTemplate redisTemplate;

    @RequestMapping("/save")
    @HystrixCommand(fallbackMethod = "saveOrderFail")
    public Object save(@RequestParam("user_id") int userId, @RequestParam("product_id") int productId, HttpServletRequest request){

        String token=request.getHeader("token");
        String cookie=request.getHeader("cookie");
        System.out.println("token="+token);
        System.out.println("cookie="+cookie);



        Map<String,Object> msg=new HashMap<>();
        msg.put("code",0);
        msg.put("msg","成功");
        msg.put("data",productOrderService.save(userId,productId));
        return msg;
    }


    //注意,方法签名一定要和api方法一致
    private Object saveOrderFail(int userId,int productId,HttpServletRequest request){

        //监控报警
        String saveOrderKye="save-order";
        String sendValue=redisTemplate.opsForValue().get(saveOrderKye);
        final String ip=request.getRemoteAddr();
        //开线程
        new Thread(()->{
            if(StringUtils.isBlank(sendValue)){
                System.out.println("紧急短信，用户下单失败，请立刻查找原因,ip地址是="+ip);
                //发送一个http请求,调用短信服务 TODO
                redisTemplate.opsForValue().set(saveOrderKye,"save-order-fail",20,TimeUnit.SECONDS);
            }else{
                System.out.println("已经发送过短信，20秒内不重发");
            }
        }).start();



        Map<String,Object> msg=new HashMap<>();
        msg.put("code",-1);
        msg.put("msg","抢购人数太多，您被挤出来了，请稍后重试");
        return msg;
    }

}
