-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('档案列表', '2052', '1', 'archive', 'manage/archive/index', 1, 0, 'C', '0', '0', 'manage:archive:list', '#', 'admin', sysdate(), '', null, '档案列表菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('档案列表查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'manage:archive:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('档案列表新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'manage:archive:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('档案列表修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'manage:archive:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('档案列表删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'manage:archive:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('档案列表导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'manage:archive:export',       '#', 'admin', sysdate(), '', null, '');