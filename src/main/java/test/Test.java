package test;

import java.util.List;

import bean.Person;

public class Test {

    public static void main(String[] args) {
        List<Person> gamePaperTactics =
                com.alibaba.fastjson.JSON.parseArray("[]", Person.class);
    }

}
