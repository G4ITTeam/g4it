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

merge into inventory(id, name, type, organization_id, creation_date, last_update_date)
key(id)
values
(501, '03-2023', 'INFORMATION_SYSTEM', 101, TIMESTAMP '2023-04-01 12:00:00', null),
(502, '07-2023', 'INFORMATION_SYSTEM', 102, TIMESTAMP '2023-07-01 12:00:00', null)
;
merge into data_center(id, inventory_id, nom_court_datacenter, nom_long_datacenter, pue, localisation, nom_entite, line_number, input_file_name, session_date, creation_date, last_update_date)
key(id)
values
(501, 501, 'DC_Villeperdue_01', 'DC_Villeperdue_OpenStack_01', '1.0', 'France', 'testEntite', 1, 'datacenter.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(502, 501, 'Datacenter 9', 'Datacenter numéro 9', '1.2', 'France', 'G4IT', 1, 'datacenter.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(503, 502, 'Datacenter 5', 'Datacenter numéro 5', '1.1', 'France', 'G4IT', 1, 'datacenter.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null)
;
merge into equipement_physique(id, inventory_id, nom_equipement_physique, nom_entite, nom_source_donnee, modele, quantite, type, statut, pays_utilisation, utilisateur, date_achat, date_retrait, nb_coeur, nom_court_datacenter, conso_elec_annuelle, line_number, input_file_name, session_date, creation_date, last_update_date, fabricant, taille_du_disque, taille_memoire, type_de_processeur)
key(id)
values
(501, 501, 'Serveur 9', 'Sopra Steria Group', 'mockData', 'rack-server-with-hdd', '2', 'Compute', 'actif', 'FR', null, '2018-07-06', '2018-07-06', '5', 'Datacenter 9', '400', 1, 'physical_equipment.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null, null, null, null, null),
(502, 501, 'Serveur 10', 'Sopra Steria Group', 'mockData', 'rack-server-with-hdd', '3', 'Compute', 'actif', 'EN', null, '2018-07-07', '2018-07-07', '5', 'Datacenter 9', '400', 2, 'physical_equipment.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null, null, null, null, null),
(503, 501, 'Serveur 11', 'Sopra Steria Group', 'mockData', 'rack-server-with-hdd', '3', 'Compute', 'actif', 'FR', null, '2018-07-07', '2018-07-07', '5', 'Datacenter 9', '400', 3, 'physical_equipment.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null, null, null, null, null),
(504, 501, 'Serveur 12', 'Sopra Steria Group', 'mockData', 'rack-server-with-hdd', '3', 'Compute', 'actif', 'FR', null, '2018-07-07', '2018-07-07', '5', 'Datacenter 9', '400', 4, 'physical_equipment.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null, null, null, null, null),
(505, 502, 'Serveur 7', 'Sopra Steria Group', 'mockData', 'rack-server-with-hdd', '3', 'Compute', 'actif', 'FR', null, '2018-07-07', '2018-07-07', '5', 'Datacenter 5', '400', 5, 'physical_equipment.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null, null, null, null, null)
;
merge into equipement_virtuel(id, inventory_id, nom_vm, nom_equipement_physique, v_cpu, nom_entite, cluster, line_number, input_file_name, session_date, creation_date, last_update_date, type_eqv)
key(id)
values
(501, 501, 'VM 01', 'Serveur 9', '1', 'Sopra Steria Group', '', 1, 'logical_equipment.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null, 'calcul'),
(502, 501, 'VM 02', 'Serveur 9', '1', 'Sopra Steria Group', '', 2, 'logical_equipment.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null, 'calcul'),
(503, 501, 'VM 03', 'Serveur 9', '1', 'Sopra Steria Group', '', 3, 'logical_equipment.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null, 'calcul'),
(504, 501, 'VM 04', 'Serveur 9', '1', 'Sopra Steria Group', '', 4, 'logical_equipment.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null, 'calcul'),
(505, 501, 'VM 05', 'Serveur 9', '1', 'Sopra Steria Group', '', 5, 'logical_equipment.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null, 'calcul'),
(506, 501, 'VM 06', 'Serveur 10', '1', 'Sopra Steria Group', '', 6, 'logical_equipment.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null, 'calcul'),
(507, 501, 'VM 07', 'Serveur 10', '1', 'Sopra Steria Group', '', 7, 'logical_equipment.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null, 'calcul'),
(508, 501, 'VM 08', 'Serveur 10', '1', 'Sopra Steria Group', '', 8, 'logical_equipment.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null, 'calcul'),
(509, 501, 'VM 09', 'Serveur 11', '1', 'Sopra Steria Group', '', 9, 'logical_equipment.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null, 'calcul'),
(510, 501, 'VM 10', 'Serveur 11', '1', 'Sopra Steria Group', '', 10, 'logical_equipment.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null, 'calcul'),
(511, 501, 'VM 11', 'Serveur 12', '1', 'Sopra Steria Group', '', 11, 'logical_equipment.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null, 'calcul'),
(512, 502, 'VM 12', 'Serveur 7', '1', 'Sopra Steria Group', '', 12, 'logical_equipment.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null, 'calcul')
;
merge into application(id, inventory_id, nom_application, type_environnement, nom_vm, nom_equipement_physique, domaine, sous_domaine, nom_entite, line_number, input_file_name, session_date, creation_date, last_update_date)
key(id)
values
(501, 501, 'Application 1', 'Recette', 'VM 01', 'Serveur 9', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(502, 501, 'Application 2', 'DEV', 'VM 01', 'Serveur 9', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(503, 501, 'Application 3', 'PROD', 'VM 02', 'Serveur 9', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(504, 501, 'Application 4', 'Recette', 'VM 02', 'Serveur 9', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(505, 501, 'Application 5', 'Recette', 'VM 03', 'Serveur 9', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(506, 501, 'Application 6', 'Recette', 'VM 03', 'Serveur 9', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(507, 501, 'Application 7', 'Recette', 'VM 04', 'Serveur 9', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(508, 501, 'Application 8', 'Recette', 'VM 05', 'Serveur 9', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(509, 501, 'Application 9', 'PROD', 'VM 06', 'Serveur 10', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(510, 501, 'Application 10', 'Recette', 'VM 07', 'Serveur 10', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(511, 501, 'Application 11', 'Recette', 'VM 08', 'Serveur 10', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(512, 501, 'Application 12', 'Recette', 'VM 09', 'Serveur 11', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(513, 501, 'Application 13', 'Recette', 'VM 10', 'Serveur 11', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(514, 501, 'Application 14', 'Recette', 'VM 11', 'Serveur 12', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(515, 502, 'Application 15', 'Recette', 'VM 12', 'Serveur 7', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-02 02:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(516, 502, 'Application 16', 'Recette', 'VM 12', 'Serveur 7', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-02 02:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(517, 502, 'Application 17', 'Recette', 'VM 12', 'Serveur 7', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-02 02:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(518, 502, 'Application 18', 'Recette', 'VM 12', 'Serveur 7', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-02 02:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(519, 502, 'Application 19', 'Recette', 'VM 12', 'Serveur 7', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-02 02:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(520, 502, 'Application 20', 'Recette', 'VM 12', 'Serveur 7', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-02 02:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(520, 502, 'Application 20', 'Recette', 'VM 11', 'Serveur 7', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-02 02:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(520, 502, 'Application 20', 'Dev', 'VM 12', 'Serveur 7', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-02 02:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(520, 502, 'Application 20', 'Dev', 'VM 11', 'Serveur 7', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-02 02:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(520, 502, 'Application 20', 'Prod', 'VM 12', 'Serveur 7', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-02 02:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(520, 502, 'Application 20', 'Prod', 'VM 11', 'Serveur 7', 'domaine 1', 'sous domaine 1', 'Sopra Steria Group', 1, 'application.csv', TIMESTAMP '2023-04-02 02:00:00', TIMESTAMP '2023-04-01 12:00:00', null)
;
