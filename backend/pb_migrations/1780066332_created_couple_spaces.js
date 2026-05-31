/// <reference path="../pb_data/types.d.ts" />
migrate((db) => {
  const collection = new Collection({
    "id": "oueo8tnso08u9d9",
    "created": "2026-05-29 14:52:12.422Z",
    "updated": "2026-05-29 14:52:12.422Z",
    "name": "couple_spaces",
    "type": "base",
    "system": false,
    "schema": [
      {
        "system": false,
        "id": "code_field",
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
}, (db) => {
  const dao = new Dao(db);
  const collection = dao.findCollectionByNameOrId("oueo8tnso08u9d9");

  return dao.deleteCollection(collection);
})
