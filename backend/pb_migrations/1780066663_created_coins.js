/// <reference path="../pb_data/types.d.ts" />
migrate((db) => {
  const collection = new Collection({
    "id": "bz6yweocmz5e2se",
    "created": "2026-05-29 14:57:43.942Z",
    "updated": "2026-05-29 14:57:43.942Z",
    "name": "coins",
    "type": "base",
    "system": false,
    "schema": [
      {
        "system": false,
        "id": "cn_sp",
        "name": "space_id",
        "type": "relation",
        "required": true,
        "presentable": false,
        "unique": false,
        "options": {
          "collectionId": "mg5v4ust9tvr522",
          "cascadeDelete": true,
          "minSelect": null,
          "maxSelect": 1,
          "displayFields": null
        }
      },
      {
        "system": false,
        "id": "cn_bl",
        "name": "balance",
        "type": "number",
        "required": false,
        "presentable": false,
        "unique": false,
        "options": {
          "min": 0,
          "max": null,
          "noDecimal": false
        }
      },
      {
        "system": false,
        "id": "cn_te",
        "name": "total_earned",
        "type": "number",
        "required": false,
        "presentable": false,
        "unique": false,
        "options": {
          "min": 0,
          "max": null,
          "noDecimal": false
        }
      },
      {
        "system": false,
        "id": "cn_ts",
        "name": "total_spent",
        "type": "number",
        "required": false,
        "presentable": false,
        "unique": false,
        "options": {
          "min": 0,
          "max": null,
          "noDecimal": false
        }
      }
    ],
    "indexes": [],
    "listRule": "space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id",
    "viewRule": "space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id",
    "createRule": "@request.auth.id != \"\"",
    "updateRule": "space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id",
    "deleteRule": null,
    "options": {}
  });

  return Dao(db).saveCollection(collection);
}, (db) => {
  const dao = new Dao(db);
  const collection = dao.findCollectionByNameOrId("bz6yweocmz5e2se");

  return dao.deleteCollection(collection);
})
