package com.iams.manage.domain;

import java.util.Set;

public class UserGroups {

    public static final String AUDITOR = "auditor";
    public static final String DIRECTOR = "director";
    public static final String ADMIN = "ceo";
//    public static final String USER = "user";

    public static final Set<String> ALL_GROUPS = Set.of(AUDITOR, DIRECTOR, ADMIN);
}
