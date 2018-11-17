package cn.zlihj.web;

import cn.zlihj.dto.Result;
import cn.zlihj.util.LoginContext;
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
    @ResponseBody
    public Result checkLogin() {
        Result result = Result.successResult();
        result.setData(LoginContext.currentUser());
        return result;
    }
}
