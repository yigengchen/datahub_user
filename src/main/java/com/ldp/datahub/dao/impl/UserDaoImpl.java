package com.ldp.datahub.dao.impl;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import com.ldp.datahub.common.Constant;
import com.ldp.datahub.dao.BaseJdbcDao;
import com.ldp.datahub.dao.UserDao;
import com.ldp.datahub.entity.User;

@Repository
public class UserDaoImpl extends BaseJdbcDao implements UserDao
{
	private static boolean createdTable = false;
	public static String tableName="DH_USER";

	@Override
	public User getUser(String loginName) 
	{
		checkAndCreateTable();
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM DH_USER WHERE LOGIN_NAME=? AND USER_STATUS<?");
		List<User> users =  getJdbcTemplate().query(sql.toString(), new Object[]{loginName,Constant.userStatus.DESTROY}, BeanPropertyRowMapper.newInstance(User.class));
		if(users.isEmpty()){
			return null;
		}
		return users.get(0);
	}
	
	@Override
	public int insertUser(User user) {
		checkAndCreateTable();
		
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO DH_USER (LOGIN_NAME,LOGIN_PASSWD,USER_STATUS,USER_TYPE,NICK_NAME,OP_TIME,USER_NAME,SUMMARY");
		if(user.getUserId()!=0){
			sql.append(",USER_ID");
		}
		sql.append(")");
		if(user.getUserId()!=0){
			sql.append(" VALUES(?,?,?,?,?,?,?,?,?)");
		}else{
			sql.append(" VALUES(?,?,?,?,?,?,?,?)");
		}
		
		List<Object> param = new ArrayList<Object>();
		param.add(user.getLoginName());
		param.add(user.getLoginPasswd());
		param.add(user.getUserStatus());
		param.add(user.getUserType());
		param.add(user.getNickName());
		param.add(user.getOpTime());
		param.add(user.getUserName());
		param.add(user.getSummary());
		if(user.getUserId()!=0){
			param.add(user.getUserId());
		}
		
		return save(sql.toString(), param.toArray());
	}
	
	@Override
	public boolean isExist(String loginName){
		checkAndCreateTable();
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT LOGIN_NAME FROM DH_USER WHERE LOGIN_NAME=? AND USER_STATUS<?");
		return getJdbcTemplate().query(sql.toString(), new Object[]{loginName,Constant.userStatus.DESTROY}, BeanPropertyRowMapper.newInstance(String.class)).size()>0;
	}
	
	@Override
	public String getPwd(String loginName){
		checkAndCreateTable();
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT LOGIN_PASSWD FROM DH_USER WHERE LOGIN_NAME=? AND USER_STATUS<?");
		return getJdbcTemplate().queryForObject(sql.toString(), new Object[]{loginName,Constant.userStatus.DESTROY}, String.class);
	}
	
	@Override
	public void delete(int id){
		
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM DH_USER WHERE USER_ID=?");
		getJdbcTemplate().update(sql.toString(),new Object[]{id});
	}
	
	@Override
	public void updateStatus(String loginName, int status) {
		
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE DH_USER SET USER_STATUS=? WHERE LOGIN_NAME=? AND ");
		if(status==Constant.userStatus.DESTROY){
			sql.append(" USER_STATUS!=?");
		}else {
			sql.append(" USER_STATUS<?");
		}
		Object[] args = new Object[]{status,loginName,Constant.userStatus.DESTROY};
		getJdbcTemplate().update(sql.toString(),args);
		
		
	}
	
	/**
	 * 根据用户名修改用户
	 */
	@Override
	public void updateUser(User user)
	{

		List<Object> args = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE DH_USER SET ");
		
		if(StringUtils.isNotEmpty(user.getUserName())){
			sql.append(" USER_NAME=?,");
			args.add(user.getUserName());
		}
		if(StringUtils.isNotEmpty(user.getLoginPasswd())){
			sql.append(" LOGIN_PASSWD=?,");
			args.add(user.getLoginPasswd());
		}
		if(StringUtils.isNotEmpty(user.getNickName())){
			sql.append(" NICK_NAME=?,");
			args.add(user.getNickName());
		}
		if(StringUtils.isNotEmpty(user.getSummary())){
			sql.append(" SUMMARY=?,");
			args.add(user.getSummary());
		}
		if(user.getUserStatus()>0){
			sql.append(" USER_STATUS=?,");
			args.add(user.getUserStatus());
		}
		if(user.getUserType()>0){
			sql.append(" USER_TYPE=?,");
			args.add(user.getUserType());
		}
		sql.append(" OP_TIME=?");
		args.add(new Timestamp(System.currentTimeMillis()));
		
		sql.append(" WHERE LOGIN_NAME=? ");
		
		if(user.isDestoryed()){
			sql.append("AND USER_STATUS=?");
		}else{
			sql.append("AND USER_STATUS<?");
		}
		args.add(user.getLoginName());
		args.add(Constant.userStatus.DESTROY);
		
		getJdbcTemplate().update(sql.toString(),args.toArray());
		
	}


	@Override
	public void delete(String loginName) {
		
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM DH_USER WHERE LOGIN_NAME=?");
		getJdbcTemplate().update(sql.toString(),new Object[]{loginName});
		
	}

	@Override
	public void updatePwd(String loginName, String pwd) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE DH_USER SET LOGIN_PASSWD=? WHERE LOGIN_NAME=? AND USER_STATUS<?");
		Object[] args = new Object[]{pwd,loginName,Constant.userStatus.DESTROY};
		getJdbcTemplate().update(sql.toString(),args);
		
	}

	@Override
	public int getUserId(String loginName) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT USER_ID FROM DH_USER WHERE LOGIN_NAME=? AND USER_STATUS<?");
		
		return getJdbcTemplate().queryForObject(sql.toString(), new Object[]{loginName,Constant.userStatus.DESTROY}, Integer.class);
	}

	private void checkAndCreateTable(){
		if(createdTable){
			return;
		}
		StringBuilder sql = new StringBuilder();
		try {
			sql.append("SELECT USER_ID FROM DH_USER LIMIT 1");
			getJdbcTemplate().queryForObject(sql.toString(),Integer.class);
		} catch (Exception e) {
			String msg = e.getMessage();
			if(msg.contains("Table 'datahub.DH_USER' doesn't exist")){
				creatTable();
			}
		}
		createdTable = true;
	}
	private void creatTable(){
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE DH_USER");
		sql.append("(");
		sql.append("USER_ID	INT NOT NULL,");
		sql.append("USER_STATUS	INT NOT NULL,");
		sql.append("USER_TYPE	INT COMMENT '1-普通；2-管理员',");
		sql.append("USER_NAME	VARCHAR(64) COMMENT '登录用户名',");
		sql.append("NICK_NAME	VARCHAR(1024),");
		sql.append("LOGIN_NAME	VARCHAR(64),");
		sql.append("LOGIN_PASSWD	VARCHAR(1024) COMMENT '密码，需密文存储',");
		sql.append("SUMMARY	VARCHAR(1024),");
		sql.append("OP_TIME	TIMESTAMP,");
		sql.append("PRIMARY KEY (USER_ID)");
		sql.append(");");
		getJdbcTemplate().execute(sql.toString());
		
		String alterSql = "ALTER TABLE DH_USER MODIFY USER_ID INT UNSIGNED NOT NULL AUTO_INCREMENT;";
		getJdbcTemplate().execute(alterSql);
	}

	

}
