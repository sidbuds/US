/// <reference path="../pb_data/types.d.ts" />
migrate((db) => {
  const collection = new Collection({
    "id": "4t952azc8i93as4",
    "created": "2026-05-29 14:57:43.960Z",
    "updated": "2026-05-29 14:57:43.960Z",
    "name": "wishlist",
    "type": "base",
    "system": false,
    "schema": [
      {
        "system": false,
        "id": "wl_sp",
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
        "id": "wl_nm",
        "name": "name",
        "type": "text",
        "required": true,
        "presentable": false,
        "unique": false,
        "options": {
          "min": 1,
          "max": 100,
          "pattern": ""
        }
      },
      {
        "system": false,
        "id": "wl_tc",
        "name": "target_coins",
        "type": "number",
        "required": true,
        "presentable": false,
        "unique": false,
        "options": {
          "min": 1,
          "max": null,
          "noDecimal": false
        }
      },
      {
        "system": false,
        "id": "wl_cb",
        "name": "current_balance",
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
        "id": "wl_iu",
        "name": "image_url",
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
        "id": "wl_st",
        "name": "status",
        "type": "select",
        "required": false,
        "presentable": false,
        "unique": false,
        "options": {
          "maxSelect": 1,
          "values": [
            "active",
            "completed"
          ]
        }
      }
    ],
    "indexes": [],
    "listRule": "space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id",
    "viewRule": "space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id",
    "createRule": "@request.auth.id != \"\"",
    "updateRule": "space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id",
    "deleteRule": "space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id",
    "options": {}
  });

  return Dao(db).saveCollection(collection);
}, (db) => {
  const dao = new Dao(db);
  const collection = dao.findCollectionByNameOrId("4t952azc8i93as4");

  return dao.deleteCollection(collection);
})
