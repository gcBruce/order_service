package net.xdclass.order_service.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import net.xdclass.order_service.domain.ProductOrder;
import net.xdclass.order_service.service.ProductClient;
import net.xdclass.order_service.service.ProductOrderService;
import net.xdclass.order_service.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class ProductOrderServiceImpl implements ProductOrderService {

    //ribbon
    //@Autowired
    //private RestTemplate restTemplate;

    //feign
    @Autowired
    public ProductClient productClient;

    @Override
    public ProductOrder save(int userId, int productId) {
        //ribbon
        //获取商品详情
        //Map<String,Object> productMap =restTemplate.getForObject("http://product-service/api/v1/product/find?id="+productId,Map.class);

        //feign(推荐)
        //获取商品详情
        String response=productClient.findById(productId);
        JsonNode jsonNode = JsonUtils.str2JsonNode(response);

        //调用用户服务，主要是获取用户名称，用户的级别或者积分信息
        //TODO

        ProductOrder productOrder=new ProductOrder();
        productOrder.setCreateTime(new Date());
        productOrder.setUserId(userId);
        productOrder.setTradeNo(UUID.randomUUID().toString());
        //productOrder.setProductName(productMap.get("name").toString());
        productOrder.setProductName(jsonNode.get("name").toString());
        //productOrder.setPrice(Integer.valueOf(productMap.get("price").toString()));
        productOrder.setPrice(Integer.valueOf(jsonNode.get("price").toString()));
        return productOrder;
    }
}
