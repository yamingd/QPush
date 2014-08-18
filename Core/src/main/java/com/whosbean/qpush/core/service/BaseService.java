package com.whosbean.qpush.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Properties;

/**
 * Created by yaming_deng on 14-8-8.
 */
public abstract class BaseService implements InitializingBean {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("mainJdbc")
    protected JdbcTemplate mainJdbc;

    @Autowired
    @Qualifier("mainJdbcNamed")
    protected NamedParameterJdbcTemplate mainJdbcNamed;

    @Autowired
    protected Properties appConfigs;
}
