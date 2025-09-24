-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('借阅记录', '2006', '1', 'record', 'manage/record/index', 1, 0, 'C', '0', '0', 'manage:record:list', '#', 'admin', sysdate(), '', null, '借阅记录菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('借阅记录查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'manage:record:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('借阅记录新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'manage:record:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('借阅记录修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'manage:record:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('借阅记录删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'manage:record:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('借阅记录导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'manage:record:export',       '#', 'admin', sysdate(), '', null, '');