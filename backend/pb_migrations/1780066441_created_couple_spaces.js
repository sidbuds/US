/// <reference path="../pb_data/types.d.ts" />
migrate((db) => {
  const collection = new Collection({
    "id": "mg5v4ust9tvr522",
    "created": "2026-05-29 14:54:01.235Z",
    "updated": "2026-05-29 14:54:01.235Z",
    "name": "couple_spaces",
    "type": "base",
    "system": false,
    "schema": [
      {
        "system": false,
        "id": "cs_code",
        "name": "code",
        "type": "text",
        "required": true,
        "presentable": false,
        "unique": false,
        "options": {
          "min": 6,
          "max": 6,
          "pattern": ""
        }
      },
      {
        "system": false,
        "id": "cs_ua",
        "name": "user_a_id",
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
        "id": "cs_ub",
        "name": "user_b_id",
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
        "id": "cs_st",
        "name": "status",
        "type": "select",
        "required": false,
        "presentable": false,
        "unique": false,
        "options": {
          "maxSelect": 1,
          "values": [
            "active",
            "disabled"
          ]
        }
      }
    ],
    "indexes": [
      "CREATE UNIQUE INDEX `idx_cs_code` ON `couple_spaces` (`code`) WHERE status = 'active'"
    ],
    "listRule": "user_a_id = @request.auth.id || user_b_id = @request.auth.id",
    "viewRule": "user_a_id = @request.auth.id || user_b_id = @request.auth.id",
    "createRule": "@request.auth.id != ''",
    "updateRule": "user_a_id = @request.auth.id || user_b_id = @request.auth.id",
    "deleteRule": null,
    "options": {}
  });

  return Dao(db).saveCollection(collection);
}, (db) => {
  const dao = new Dao(db);
  const collection = dao.findCollectionByNameOrId("mg5v4ust9tvr522");

  return dao.deleteCollection(collection);
})
