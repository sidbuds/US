/// <reference path="../pb_data/types.d.ts" />
migrate((db) => {
  const collection = new Collection({
    "id": "gy5jwamh8u0xqfh",
    "created": "2026-05-29 14:55:46.897Z",
    "updated": "2026-05-29 14:55:46.897Z",
    "name": "interactions",
    "type": "base",
    "system": false,
    "schema": [
      {
        "system": false,
        "id": "it_sp",
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
        "id": "it_fu",
        "name": "from_user_id",
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
        "id": "it_tu",
        "name": "to_user_id",
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
        "id": "it_tp",
        "name": "type",
        "type": "select",
        "required": true,
        "presentable": false,
        "unique": false,
        "options": {
          "maxSelect": 1,
          "values": [
            "hug",
            "kiss",
            "miss"
          ]
        }
      },
      {
        "system": false,
        "id": "it_rn",
        "name": "reason",
        "type": "text",
        "required": false,
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
        "id": "it_ir",
        "name": "is_read",
        "type": "bool",
        "required": false,
        "presentable": false,
        "unique": false,
        "options": {}
      }
    ],
    "indexes": [],
    "listRule": "space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id",
    "viewRule": "space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id",
    "createRule": "@request.auth.id != ''",
    "updateRule": "to_user_id = @request.auth.id",
    "deleteRule": null,
    "options": {}
  });

  return Dao(db).saveCollection(collection);
}, (db) => {
  const dao = new Dao(db);
  const collection = dao.findCollectionByNameOrId("gy5jwamh8u0xqfh");

  return dao.deleteCollection(collection);
})
