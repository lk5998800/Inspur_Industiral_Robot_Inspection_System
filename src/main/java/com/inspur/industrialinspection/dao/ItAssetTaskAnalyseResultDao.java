package com.inspur.industrialinspection.dao;

/**
 * 资产盘点任务分析结果
 * @author kliu
 * @date 2022/8/2 15:54
 */
public interface ItAssetTaskAnalyseResultDao {
    /**
     * rfid正常资产
     * @param instanceId
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/8/5 15:27
     */
    void saveItAssetRfidNormal(long instanceId, long roomId);
    /**
     * rfid资产丢失
     * @param instanceId
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/8/5 15:27
     */
    void saveItAssetRfidLack(long instanceId, long roomId);
    /**
     * rfid不明资产
     * @param instanceId
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/8/5 15:27
     */
    void saveItAssetRfidUnknown(long instanceId, long roomId);
    /**
     * qrcode正常资产
     * @param instanceId
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/8/5 15:27
     */
    void saveItAssetQrcodeNormal(long instanceId, long roomId);
    /**
     * qrcode资产丢失
     * @param instanceId
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/8/5 15:28
     */
    void saveItAssetQrcodeLack(long instanceId, long roomId);
    /**
     * qrcode资产不明
     * @param instanceId
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/8/5 15:28
     */
    void saveItAssetQrcodeUnknown(long instanceId, long roomId);
    /**
     * qrcode正常移位
     * @param instanceId
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/8/5 15:28
     */
    void saveItAssetQrcodeShift(long instanceId, long roomId);

    /**
     * 判断实例是否正常
     * @param instanceId
     * @return boolean
     * @author kliu
     * @date 2022/8/5 15:28
     */
    boolean instanceNoraml(long instanceId);
}
