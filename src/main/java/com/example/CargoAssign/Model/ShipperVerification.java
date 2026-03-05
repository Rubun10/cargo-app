package com.example.CargoAssign.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="shipper_verification")
public class ShipperVerification {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user; 
	 
	@Column(name = "aadhar_name", nullable = false)
	private String aadharName;
	
	@Column(name = "aadhar_number", nullable = false, unique = true, length = 12)
	private String aadharNumber;
	
	@Column(name="aadhar_image", nullable=false)
	private String aadharImage;
	
	@Column(name="user_photo", nullable=false)
	private String userPhoto;
	
	@Column(name="company_name", nullable=false)
	private String companyName;
	
	@Column(name="gstin", nullable=false)
	private String gstin;
	
	@Column(name="business_email", nullable=false)
	private String businessEmail;
	
	@Column(name="business_mobile", nullable=false)
	private String businessMobile;
	
	private String address;
	private String city;
	private String state;
	private int pincode;
	
	/* ================= STATUS ================= */
	@Column(length = 20)
	private String status = "PENDING";
	// PENDING | APPROVED | REJECTED

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
	
}
