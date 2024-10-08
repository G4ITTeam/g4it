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
(301, '03-2023', 'INFORMATION_SYSTEM', 101, TIMESTAMP '2023-04-01 12:00:00', null),
(302, '07-2023', 'INFORMATION_SYSTEM', 102, TIMESTAMP '2023-07-01 12:00:00', null)
;
merge into data_center(id, inventory_id, nom_court_datacenter, nom_long_datacenter, pue, localisation, nom_entite, line_number, input_file_name, session_date, creation_date, last_update_date)
key(id)
values
(301, 301, 'DC_Villeperdue_01', 'DC_Villeperdue_OpenStack_01', '1.0', 'France', 'testEntite', 1, 'datacenter.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(302, 301, 'Datacenter 9', 'Datacenter numéro 9', '1.2', 'France', 'G4IT', 1, 'datacenter.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null),
(303, 302, 'Datacenter 5', 'Datacenter numéro 5', '1.1', 'France', 'G4IT', 1, 'datacenter.csv', TIMESTAMP '2023-04-01 12:00:00', TIMESTAMP '2023-04-01 12:00:00', null)
;
merge into equipement_physique(id, inventory_id, nom_equipement_physique, nom_entite, nom_source_donnee, modele, quantite, type, statut, pays_utilisation, utilisateur, date_achat, date_retrait, nb_coeur, nom_court_datacenter, conso_elec_annuelle, line_number, input_file_name, session_date, creation_date, last_update_date, fabricant, taille_du_disque, taille_memoire, type_de_processeur)
key(id)
values
(301, 301, 'Serveur 9', 'Sopra Steria Group', 'mockData', 'rack-server-with-hdd', '2', 'Compute', 'actif', 'FR', null, '2018-07-06', '2018-07-06', '5', 'DC_Villeperdue_01', '400', 1, 'physical_equipment.csv', TIMESTAMP '2023-04-01 02:00:00', TIMESTAMP '2023-04-01 12:00:00', null, null, null, null, null),
(302, 301, 'Serveur 10', 'Sopra Steria Group', 'mockData', 'rack-server-with-hdd', '3', 'Compute', 'actif', 'EN', null, '2018-07-07', '2018-07-07', '5', 'Datacenter 9', '400', 2, 'physical_equipment.csv', TIMESTAMP '2023-04-01 02:00:00', TIMESTAMP '2023-04-01 12:00:00', null, null, null, null, null),
(303, 301, 'Serveur 11', 'Sopra Steria Group', 'mockData', 'rack-server-with-hdd', '3', 'Compute', 'actif', 'FR', null, '2018-07-07', '2018-07-07', '5', 'Datacenter 9', '400', 3, 'physical_equipment.csv', TIMESTAMP '2023-04-01 02:00:00', TIMESTAMP '2023-04-01 12:00:00', null, null, null, null, null),
(304, 301, 'Serveur 12', 'Sopra Steria Group', 'mockData', 'rack-server-with-hdd', '3', 'Compute', 'actif', 'FR', null, '2018-07-07', '2018-07-07', '5', 'Datacenter 9', '400', 4, 'physical_equipment.csv', TIMESTAMP '2023-04-01 02:00:00', TIMESTAMP '2023-04-01 12:00:00', null, null, null, null, null),
(305, 302, 'Serveur 7', 'Sopra Steria Group', 'mockData', 'rack-server-with-hdd', '3', 'Compute', 'actif', 'FR', null, '2018-07-07', '2018-07-07', '5', 'Datacenter 5', '400', 5, 'physical_equipment.csv', TIMESTAMP '2023-04-02 02:00:00', TIMESTAMP '2023-04-01 12:00:00', null, null, null, null, null)
;
