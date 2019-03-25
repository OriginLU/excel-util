package com.chl.aviator.test;

import com.chl.jdbc.extension.annotation.Condition;
import com.chl.jdbc.extension.annotation.Conditions;
import com.chl.jdbc.extension.annotation.Select;

import java.util.List;

/**
 * @author lch
 * @since 2019-03-23
 */
public interface TestSQL {





    @Conditions(name = "testid",value = {
            @Condition(expression = "$(startTime)",condition = "startTime < 0")
    })
    void commonCondition();



    @Select(baseSQL = "select * from dual",refCondition = "testid")
    @Conditions({
            @Condition(expression = "$(startTime)",condition = "startTime > 0")
    })
    List<String> testDemo();
}
