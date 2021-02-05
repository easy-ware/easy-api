package com.github.easyware.easyapiapp;

import com.github.easyware.easyapiapp.domain.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 测试类名
 */
@RestController
public class TestController {

    private User user;

    /*
     * name0
     */
     private String name; //name1

    /**
     * this  is  find OnsaleByPage. can you
     * note this?
     * no no no no
     * @param index param_index <br> <p>PPP</p>
     *               this is index
     * @param counter param_counter
     * @return a map object
     *
     */
    @RequestMapping("/findOnsaleByPage")
    public Map<String, Object> findOnsaleByPage(Integer index, Integer counter) {
        return null;
    }

}
