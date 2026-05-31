/// <reference path="../pb_data/types.d.ts" />
migrate((db) => {
  const collection = new Collection({
    "id": "2homh3s6yf3frq7",
    "created": "2026-05-29 14:57:43.954Z",
    "updated": "2026-05-29 14:57:43.954Z",
    "name": "expenses",
    "type": "base",
    "system": false,
    "schema": [
      {
        "system": false,
        "id": "ex_sp",
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
        "id": "ex_am",
        "name": "amount",
        "type": "number",
        "required": true,
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
        "id": "ex_ct",
        "name": "category",
        "type": "select",
        "required": true,
        "presentable": false,
        "unique": false,
        "options": {
          "maxSelect": 1,
          "values": [
            "food",
            "shopping",
            "travel",
            "housing",
            "entertainment",
            "other",
            "income"
          ]
        }
      },
      {
        "system": false,
        "id": "ex_nt",
        "name": "note",
        "type": "text",
        "required": false,
        "presentable": false,
        "unique": false,
        "options": {
          "min": null,
          "max": 500,
          "pattern": ""
        }
      },
      {
        "system": false,
        "id": "ex_pb",
        "name": "paid_by",
        "type": "text",
        "required": true,
        "presentable": false,
        "unique": false,
        "options": {
          "min": null,
          "max": null,
          "pattern": ""
        }
      },
      {
        "system": false,
        "id": "ex_ii",
        "name": "is_income",
        "type": "bool",
        "required": false,
        "presentable": false,
        "unique": false,
        "options": {}
      },
      {
        "system": false,
        "id": "ex_dt",
        "name": "date",
        "type": "text",
        "required": true,
        "presentable": false,
        "unique": false,
        "options": {
          "min": null,
          "max": null,
          "pattern": ""
        }
      }
    ],
    "indexes": [],
    "listRule": "space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id",
    "viewRule": "space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id",
    "createRule": "@request.auth.id != \"\"",
    "updateRule": "paid_by = @request.auth.id",
    "deleteRule": "paid_by = @request.auth.id",
    "options": {}
  });

  return Dao(db).saveCollection(collection);
}, (db) => {
  const dao = new Dao(db);
  const collection = dao.findCollectionByNameOrId("2homh3s6yf3frq7");

  return dao.deleteCollection(collection);
})
