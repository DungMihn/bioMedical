/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.bioMedical.form;

/**
 *
 * @author ADMIN
 */
public class SlotDto {
    private Long slotId;
    private String time;

    public SlotDto() {}
    public SlotDto(Long slotId, String time) {
        this.slotId = slotId;
        this.time = time;
    }

    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}