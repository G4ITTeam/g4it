merge into g4it_subscriber(id, name, creation_date, last_update_date) key(id) values
(100, 'SSG', '2023-03-28 10:11:10', '2023-03-28 10:11:10'),
(101, 'OTHER', '2023-03-28 10:11:10', '2023-03-28 10:11:10')
;

merge into g4it_organization(id, name, subscriber_id, creation_date, last_update_date) key(id) values
(101, 'G4IT', 100, '2023-03-28 10:11:10', '2023-03-28 10:11:10'),
(102, 'OTHER', 100, '2023-03-28 10:11:10', '2023-03-28 10:11:10'),
(103, 'SUB_OTHER_1', 101, '2023-03-28 10:11:10', '2023-03-28 10:11:10'),
(104, 'SUB_OTHER_2', 101, '2023-03-28 10:11:10', '2023-03-28 10:11:10')
;

merge into g4it_user(id, email, creation_date, last_update_date) key(id) values
(100, 'username@organization.com', '2023-03-28 10:11:10', '2023-03-28 10:11:10')
;

merge into g4it_user_organization(id, user_id, organization_id, default_flag) values
(100, 100, 101, true)
;

INSERT INTO public.digital_service(uid, name, user_id, organization_id) VALUES ('uid', 'my service test', 100, 101);

INSERT INTO public.ref_Critere VALUES ('Acidification',	'Air acidification is linked to emissions of nitrogen oxides, sulfur oxides, ammonia and hydrochloric acid. These pollutants turn into acids in the presence of moisture, and their fallout can damage ecosystems as well as buildings.',	'mol H+ eq');
INSERT INTO public.ref_Critere VALUES ('Climate change',	'Greenhouse gases (GHG) are gaseous components which absorb the infrared radiation emitted by the earths surface 1 and thus contribute to the greenhouse effect.',	'kg CO2 eq');
INSERT INTO public.ref_Critere VALUES ('Ionising radiation',	'Corresponds to the effects of radioactivity. This impact corresponds to the radioactive waste resulting from the production of nuclear electricity.',	'kBq U-235 eq');
INSERT INTO public.ref_Critere VALUES ('Particulate matter and respiratory inorganics',	'The presence of small-diameter fine particles in the air - especially those with a diameter of less than 10 microns - represents a human health problem, since their inhalation can cause respiratory and cardiovascular problems.',	'Disease incidence');
INSERT INTO public.ref_Critere VALUES ('Resource use (minerals and metals)',	'Industrial exploitation leads to a decrease in available resources whose reserves are limited. This indicator assesses the amount of mineral and metallic resources extracted from nature as if they were antimony.',	'kg Sb eq');

INSERT INTO public.server_characteristic VALUES (1,'vCPU',96, NULL, NULL);

INSERT INTO public.datacenter_digital_service VALUES ('4062bb27-b104-4e66-9cd0-244b0b5875ab','uid','Default DC','France',1.5,'2023-09-27 17:46:59.888462','2023-09-27 17:46:59.888462');

INSERT INTO public.server VALUES ('uid2','uid',1,'4062bb27-b104-4e66-9cd0-244b0b5875ab',2,'Server A','DEDICATED','COMPUTE',1,5,16513,8760,'2023-09-27 17:48:04.566861','2023-09-27 17:48:04.566861');

INSERT INTO public.virtual_equipment_characteristic VALUES (1, 'VM',4.0,NULL,NULL);

INSERT INTO public.virtual_equipment_digital_service VALUES ('uidVm','uid2',1,'VM',1,400.0,NULL,NULL);

INSERT INTO public.ind_indicateur_impact_equipement_virtuel VALUES ('2023-09-28 17:44:23.003',NULL,'uid','FIN_DE_VIE','Climate change','SSG',NULL,'uid2','uidVm',NULL,NULL,'OK','trace','1.1',20832309.3,'kg CO2 eq',NULL,NULL,'1970-01-01','SSG','','');
INSERT INTO public.ind_indicateur_impact_equipement_virtuel VALUES ('2023-09-28 17:44:23.003',NULL,'uid','FIN_DE_VIE','Particulate matter and respiratory inorganics','SSG',NULL,'uid2','uidVm',NULL,NULL,'OK','trace','1.1',1.068294264,'Disease incidence',NULL,NULL,'1970-01-01','SSG','','');
INSERT INTO public.ind_indicateur_impact_equipement_virtuel VALUES ('2023-09-28 17:44:23.003',NULL,'uid','FIN_DE_VIE','Ionising radiation','SSG',NULL,'uid2','uidVm',NULL,NULL,'OK','trace','1.1',1579246.23,'kBq U-235 eq',NULL,NULL,'1970-01-01','SSG','','');
INSERT INTO public.ind_indicateur_impact_equipement_virtuel VALUES ('2023-09-28 17:44:23.003',NULL,'uid','FIN_DE_VIE','Resource use (minerals and metals)','SSG',NULL,'uid2','uidVm',NULL,NULL,'OK','trace','1.1',39.0259095,'kg Sb eq',NULL,NULL,'1970-01-01','SSG','','');
INSERT INTO public.ind_indicateur_impact_equipement_virtuel VALUES ('2023-09-28 17:44:23.003',NULL,'uid','FIN_DE_VIE','Acidification','SSG',NULL,'uid2','uidVm',NULL,NULL,'OK','trace','1.1',317649.864,'mol H+ eq',NULL,NULL,'1970-01-01','SSG','','');

INSERT INTO public.ind_indicateur_impact_equipement_physique VALUES ('2023-09-29 14:16:06.965', '2023-06-01', 'FIN_DE_VIE',	'Acidification',NULL,'OK', 'Version_Calcule',	'1.0', NULL,	0.004625000000000001, 1,NULL, 'Dedicated Server', 'mol H+ eq', NULL, 'SSG',	'uid2','uid',NULL, 	'1970-01-01',	'SSG', '', '');
INSERT INTO public.ind_indicateur_impact_equipement_physique VALUES ('2023-09-29 14:16:06.965', '2023-06-01', 'FIN_DE_VIE',	'Climate change',NULL, 'OK',	'Version_Calcule',	'1.0',NULL,	0.30833333333333335, 1, NULL,	'Dedicated Server',	'kg CO2 eq', NULL, 'SSG','uid2','uid',NULL,		'1970-01-01',	'SSG', '', '');
INSERT INTO public.ind_indicateur_impact_equipement_physique VALUES ('2023-09-29 14:16:06.965', '2023-06-01', 'FIN_DE_VIE',	'Ionising radiation',NULL, 'OK',	'Version_Calcule',	'1.0',NULL,	0.03895833333333333, 1,NULL, 'Dedicated Server',	'kBq U-235 eq', NULL, 'SSG','uid2','uid',NULL, 	'1970-01-01',	'SSG', '', '');
INSERT INTO public.ind_indicateur_impact_equipement_physique VALUES ('2023-09-29 14:16:06.965', '2023-06-01', 'FIN_DE_VIE',	'Particulate matter and respiratory inorganics',NULL, 'OK',	'Version_Calcule',	'1.0', NULL, 1.6125, 1, NULL, 'Dedicated Server', 'Disease incidence', NULL, 'SSG','uid2','uid',NULL,	'1970-01-01','SSG', '', '');
INSERT INTO public.ind_indicateur_impact_equipement_physique VALUES ('2023-09-29 14:16:06.965', '2023-06-01', 'FIN_DE_VIE',	'Resource use (minerals and metals)',NULL, 'OK',	'Version_Calcule',	'1.0',NULL,	7.979166666666666,	1, NULL, 'Dedicated Server', 'kg Sb eq', NULL, 'SSG', 'uid2','uid',NULL, 	'1970-01-01',	'SSG', '', '');
