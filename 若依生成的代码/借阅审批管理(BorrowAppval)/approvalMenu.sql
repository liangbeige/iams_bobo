-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('借阅审批', '2100', '1', 'approval', 'manage/approval/index', 1, 0, 'C', '0', '0', 'manage:approval:list', '#', 'admin', sysdate(), '', null, '借阅审批菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('借阅审批查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'manage:approval:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('借阅审批新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'manage:approval:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('借阅审批修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'manage:approval:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('借阅审批删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'manage:approval:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('借阅审批导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'manage:approval:export',       '#', 'admin', sysdate(), '', null, '');