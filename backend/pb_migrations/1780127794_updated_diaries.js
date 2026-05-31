/// <reference path="../pb_data/types.d.ts" />
migrate((db) => {
  const dao = new Dao(db)
  const collection = dao.findCollectionByNameOrId("3acmzr0ux4q8csb")

  // add
  collection.schema.addField(new SchemaField({
    "system": false,
    "id": "dy_if",
    "name": "image_files",
    "type": "file",
    "required": false,
    "presentable": false,
    "unique": false,
    "options": {
      "mimeTypes": [
        "image/jpeg",
        "image/png",
        "image/webp",
        "image/gif"
      ],
      "thumbs": [
        "100x100",
        "300x300",
        "800x800"
      ],
      "maxSelect": 9,
      "maxSize": 5242880,
      "protected": false
    }
  }))

  return dao.saveCollection(collection)
}, (db) => {
  const dao = new Dao(db)
  const collection = dao.findCollectionByNameOrId("3acmzr0ux4q8csb")

  // remove
  collection.schema.removeField("dy_if")

  return dao.saveCollection(collection)
})
