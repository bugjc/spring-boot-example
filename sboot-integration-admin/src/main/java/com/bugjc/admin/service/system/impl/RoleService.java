package com.bugjc.admin.service.system.impl;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import com.bugjc.admin.service.system.IRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.bugjc.admin.dao.system.RoleDao;
import com.bugjc.admin.entity.system.Const;
import com.bugjc.admin.entity.system.Menu;
import com.bugjc.admin.entity.system.ReturnModel;
import com.bugjc.admin.entity.system.User;
import com.bugjc.admin.util.ParameterMap;
import com.bugjc.admin.util.RightsHelper;
import com.bugjc.admin.util.Tools;

@Slf4j
@Service
public class RoleService implements IRoleService {

	@Resource
	private RoleDao roleDao;
	
	@Resource
	private MenuService menuService;

	@Override
	public List<ParameterMap> list() {
		return roleDao.list();
	}

	/**
	 * 判断角色对各个菜单的各个权限
	 */
	@Override
	public Map<String, Object> getMenu(ParameterMap pm) {
		String qx = null;
		List<Menu> menus =null;
		String QXtype = pm.getString("qx");
		ParameterMap role = roleDao.getRoleById(pm);
		try {
			if("rights".equalsIgnoreCase(QXtype)){
				qx = role.getString("rights");
			}else if("add_qx".equalsIgnoreCase(QXtype)){
				qx = role.getString("add_qx");
			}else if("del_qx".equalsIgnoreCase(QXtype)){
				qx = role.getString("del_qx");
			}else if("edit_qx".equalsIgnoreCase(QXtype)){
				qx = role.getString("edit_qx");
			}else if("query_qx".equalsIgnoreCase(QXtype)){
				qx = role.getString("query_qx");
			}else{
				return ReturnModel.getModel("你请求的是一个冒牌接口", "failed", null);
			}			menus = menuService.getAllMenuList();
		
			for(Menu m:menus){
				List<Menu> subm = m.getSubMenu();
				if(subm != null && subm.size() > 0){
					int subNumber = subm.size();
					int index = 0;
					for(Menu sm:subm){
						boolean ishas = RightsHelper.testRights(qx, sm.getMENU_ID());
						sm.setHasMenu(ishas);
						if(ishas){
							index++;
						}
					}
					
					//判断子菜单是否全部选中
					if(subNumber == index){
						m.setHasMenu(true);
					}
				}
			}
			
		} catch (Exception e) {
			log.error("error", e);
			return ReturnModel.getModel("error", "failed", null);
		}
		return ReturnModel.getModel("ok", "success", menus);
	}
	
	@Override
	public Map<String, Object> edit(ParameterMap pm) {
		try {
			String idstr = pm.getString("ids");
			if(Tools.notEmpty(idstr)){
				String[] ids = idstr.split(",");
				BigInteger newRights = RightsHelper.sumRights(ids);
				String qxKey = pm.getString("qx");
				pm.put(qxKey,newRights);
			}
			System.out.println("role_desc="+pm.getString("role_desc"));
			pm.put("role_desc", pm.getString("role_desc").trim());
			roleDao.updateRoleQX(pm);
		} catch (Exception e) {
			log.error("error", e);
			return ReturnModel.getModel("error", "failed", null);
		}
		return ReturnModel.getModel("ok", "success", null);
	}

	@Override
	public Map<String, Object> add(ParameterMap pm,HttpSession session) {
		try {
			pm.put("user_id",((User)session.getAttribute(Const.SESSION_USER)).getUserId());
			pm.put("role_desc", pm.getString("role_desc").trim());
			roleDao.addRole(pm);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("add role error", e);
			return ReturnModel.getModel("error", "falied", null);
		}
		return ReturnModel.getModel("ok", "success", null);
	}

	@Override
	public Map<String, Object> del(String roleId) {
		try {
			if(Tools.isEmpty(roleId) || !Tools.isNumber(roleId)){
				return ReturnModel.getModel("你请求的是一个冒牌接口", "failed", null);
			}
			roleDao.delRole(roleId);
			roleDao.delUserRole(roleId);
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			e.printStackTrace();
			log.error("del role error", e);
			return ReturnModel.getModel("error", "falied", null);
		}
		return ReturnModel.getModel("ok", "success", null);
	}
}
