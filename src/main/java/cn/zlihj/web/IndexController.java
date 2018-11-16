package cn.zlihj.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
public class IndexController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/index")
    public String index(Model model) {
        return "index";
    }

    @RequestMapping("/checkLogin")
    public void checkLogin() {

    }
}
