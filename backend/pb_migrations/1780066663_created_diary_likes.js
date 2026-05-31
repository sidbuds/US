/// <reference path="../pb_data/types.d.ts" />
migrate((db) => {
  const collection = new Collection({
    "id": "wki68lw9ail7wy2",
    "created": "2026-05-29 14:57:43.930Z",
    "updated": "2026-05-29 14:57:43.930Z",
    "name": "diary_likes",
    "type": "base",
    "system": false,
    "schema": [
      {
        "system": false,
        "id": "dl_di",
        "name": "diary_id",
        "type": "relation",
        "required": true,
        "presentable": false,
        "unique": false,
        "options": {
          "collectionId": "3acmzr0ux4q8csb",
          "cascadeDelete": true,
          "minSelect": null,
          "maxSelect": 1,
          "displayFields": null
        }
      },
      {
        "system": false,
        "id": "dl_ui",
        "name": "user_id",
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
    "listRule": "@request.auth.id != \"\"",
    "viewRule": null,
    "createRule": "@request.auth.id != \"\"",
    "updateRule": null,
    "deleteRule": "user_id = @request.auth.id",
    "options": {}
  });

  return Dao(db).saveCollection(collection);
}, (db) => {
  const dao = new Dao(db);
  const collection = dao.findCollectionByNameOrId("wki68lw9ail7wy2");

  return dao.deleteCollection(collection);
})
