package server.game.data

// This class saves a data memory of objects to compare older data versions with the current one.

class DataMemory(private val maxDepth: Int) {
    private var dataList = mutableListOf<Map<String, Any?>>()
    private var changeMap = mutableMapOf<Int, Map<String, Any?>>()

    /**
     * updating the current data map
     * @param newData the new data map
     */
    fun updateData(newData: Map<String, Any?>) {
        if (dataList[dataList.size-1] != newData) {
            dataList += newData
            while (dataList.size > maxDepth) dataList.removeFirst()
            changeMap.clear()
        }
    }

    /**
     * comparing older versions (parameter "depth") with the current
     * @param depth depth = 0: getting the full current data map, depth > 0: getting the comparison between the data in the given depth and the current data
     */
    fun getChange(depth: Int): Map<String, Any?> {
        val index = dataList.size - depth - 1

        if (index < 0) return dataList.last()

        else {
            val selectedData = dataList[index]
            return if (depth == 0) selectedData
            else {
                if (changeMap[depth] == null) {
                    val currentData = dataList.last()
                    val changedData = mutableMapOf<String, Any?>()
                    currentData.forEach {
                        if (!selectedData.keys.contains(it.key) || !selectedData.values.contains(it.value))
                            changedData[it.key] = it.value
                    }
                    changeMap[depth] = changedData
                }
                changeMap[depth]!!
            }
        }
    }
}