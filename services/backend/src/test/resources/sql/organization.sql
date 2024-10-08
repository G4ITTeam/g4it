-- organizations

merge into g4it_organization(id, name, subscriber_id, creation_date, last_update_date,storage_retention_day_export
,storage_retention_day_output,data_retention_day,status,deletion_date,last_updated_by,created_by,is_migrated,criteria_is,criteria_ds) key(id) values
(1, 'G4IT', 1, '2023-03-28 10:11:10', '2023-03-28 10:11:10','15','10',0,'ACTIVE',null,null,null,true,null,null),
(2, 'OTHER', 1, '2023-03-28 10:11:10', '2023-03-28 10:11:10','15','10',0,'ACTIVE',null,null,null,true,null,null),
(3, 'SUB_OTHER_1', 1, '2023-03-28 10:11:10', '2023-03-28 10:11:10','15','10',7,'INACTIVE','2024-03-28 10:11:10',null,null,true,null,null),
(4, 'SUB_OTHER_2', 1, '2023-03-28 10:11:10', '2023-03-28 10:11:10','15','10',7,'TO_BE_DELETED','2024-03-28 10:11:10',null,null,true,null,null)
;

