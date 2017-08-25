package com.lrpshuai.picpicker.entity

import java.io.Serializable

class PicturesEntity : Serializable {

    var total: Int = 0
    var items: ArrayList<PictureEntity> = ArrayList()

    constructor(items: ArrayList<PictureEntity>, total: Int) {
        this.total = total
        this.items = items
    }

    constructor() {}
}