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

merge into g4it_user(id, username, creation_date, last_update_date) key(id) values
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

INSERT INTO public.network VALUES('uid2','uid',1,360,'2023-09-28 14:05:40.850228','2023-09-28 22:11:40.56445');

INSERT INTO public.ind_indicateur_impact_equipement_physique VALUES ('2023-09-29 14:16:06.965', '2023-06-01', 'FIN_DE_VIE',	'Acidification',NULL,'OK', 'Version_Calcule',	'1.0', NULL,	0.004625000000000001, 1,NULL, 'Network', 'mol H+ eq', NULL, 'G4IT',	'uid2','uid',NULL, 	'1970-01-01',	'G4IT', '', '');
INSERT INTO public.ind_indicateur_impact_equipement_physique VALUES ('2023-09-29 14:16:06.965', '2023-06-01', 'FIN_DE_VIE',	'Climate change',NULL, 'OK',	'Version_Calcule',	'1.0',NULL,	0.30833333333333335, 1, NULL,	'Network',	'kg CO2 eq', NULL, 'G4IT','uid2','uid',NULL,		'1970-01-01',	'G4IT', '', '');
INSERT INTO public.ind_indicateur_impact_equipement_physique VALUES ('2023-09-29 14:16:06.965', '2023-06-01', 'FIN_DE_VIE',	'Ionising radiation',NULL, 'OK',	'Version_Calcule',	'1.0',NULL,	0.03895833333333333, 1,NULL, 'Network',	'kBq U-235 eq', NULL, 'G4IT','uid2','uid',NULL, 	'1970-01-01',	'G4IT', '', '');
INSERT INTO public.ind_indicateur_impact_equipement_physique VALUES ('2023-09-29 14:16:06.965', '2023-06-01', 'FIN_DE_VIE',	'Particulate matter and respiratory inorganics',NULL, 'OK',	'Version_Calcule',	'1.0', NULL, 1.6125, 1, NULL, 'Network', 'Disease incidence', NULL, 'G4IT','uid2','uid',NULL,	'1970-01-01','G4IT', '', '');
INSERT INTO public.ind_indicateur_impact_equipement_physique VALUES ('2023-09-29 14:16:06.965', '2023-06-01', 'FIN_DE_VIE',	'Resource use (minerals and metals)',NULL, 'OK',	'Version_Calcule',	'1.0',NULL,	7.979166666666666,	1, NULL, 'Network', 'kg Sb eq', NULL, 'G4IT', 'uid2','uid',NULL, 	'1970-01-01',	'G4IT', '', '');
