/// <reference path="../pb_data/types.d.ts" />
migrate((db) => {
  const dao = new Dao(db);
  const collection = dao.findCollectionByNameOrId("qjk8w4uw51yc5sy");

  return dao.deleteCollection(collection);
}, (db) => {
  const collection = new Collection({
    "id": "qjk8w4uw51yc5sy",
    "created": "2026-05-29 14:54:41.780Z",
    "updated": "2026-05-29 14:54:41.780Z",
    "name": "test_rel",
    "type": "base",
    "system": false,
    "schema": [
      {
        "system": false,
        "id": "tr_sp",
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
        "id": "tr_ui",
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
    "listRule": null,
    "viewRule": null,
    "createRule": null,
    "updateRule": null,
    "deleteRule": null,
    "options": {}
  });

  return Dao(db).saveCollection(collection);
})
