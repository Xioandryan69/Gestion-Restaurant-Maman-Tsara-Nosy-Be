+ [X] initialiser base de données
+ [X] creera database.sql
+ [X] logique metier tout dans Service meme Page et Views et Controller appel Service structure MVC tu commprends
+ [X] donnée dans la base de donnée plus statique
+ [X] responsive les pages

+ ß[ ] utiliser graphe

|               |                                                                                                                                                              |               |                                   |      |
| ------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------- | --------------------------------- | ---- |
| module        | fonction                                                                                                                                                     | priorite      | description                       | etat |
| Unite         | -convertisseur unité                                                                                                                                        | priorité     | l kg cm³ mm g et tout            | ok   |
| Fournisseur   | inserer fournisseur crud                                                                                                                                     | fonctionnele  |                                   | ok   |
| Materielles   |                                                                                                                                                              |               |                                   | ok   |
|               |                                                                                                                                                              |               |                                   | ok   |
|               |                                                                                                                                                              |               |                                   | ok   |
| Ingredients   | Filter Ingredients par Categorie par StatutIngredients                                                                                                       | prioritaire   |                                   | ok   |
|               | S-on doit jetter les ingredients perimer EtatStok et InventaireIngredient<br />tres imporant datePeremption<br />TypeMouvemenIngredient :: tSortie           | prioritaire   |                                   | ok   |
|               | -ajouter les ingredients TypeMvtIngredients Entree et ajout dans EtatStokIngredients<br />StockIngredients<br /> MouvementCaisse Sorite:: achat ingredient  | prioritaire   |                                   | ok   |
| Plats         | filtrer plats par categorie                                                                                                                                  | fonctionnelle |                                   | ok   |
|               | inserer recettePlats Pour un plat specifique on connait theriquement les ingredients<br />TypeMvtIngredientsSortie <br />EtatStock InventaireIngredients    | prioritaire   |                                   | ok   |
| materielles   | filtrer categorie et statutMaterielles                                                                                                                       | fonctionnele  |                                   | ok   |
|               | HistoriqueMaterielles suivi prix                                                                                                                             | fonctionnel   |                                   | ok   |
|               | MaintenanceMaterielles<br /><br />HorsSerciete EtatSotoc et InvetaireMaterilles                                                                             | fonctionnel   |                                   | ok   |
| Personnels    | -filtrer par role                                                                                                                                            | fonctionnel   |                                   | ok   |
|               | salaire historique salaire fiche de paie<br />gestion payement par mois                                                                                      | fonctionnel   |                                   | ok   |
|               | gestionBonus salaire                                                                                                                                         | fonctionnel   |                                   | ok   |
| Clients       | inserer Clietns et CRUD                                                                                                                                      | fonctionnel   |                                   | ok   |
| ZoneLivraison | CRUD                                                                                                                                                         | fonctionnel   |                                   | ok   |
| Commandes     | inserer commandes mutlitple DetailsCommandes  1 commandes plusieros plats<br /><br />Mouvement Caisse                                                       | prioritaire   |                                   | ok   |
|               | factures Commandes Mouvement Caisse                                                                                                                          | prioritaire   |                                   | ok   |
| ingredients   | -crud ingredients                                                                                                                                            | orioritaire   |                                   | ok   |
|               | -historique ingredients                                                                                                                                      | prioritaire   | simulation suivi prix ingredients | ok   |
|               | -                                                                                                                                                            |               |                                   | ok   |
