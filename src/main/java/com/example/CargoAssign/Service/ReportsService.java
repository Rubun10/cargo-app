package com.example.CargoAssign.Service;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.CargoAssign.Model.PostLoad;
import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.repo.PostLoadRepo;

@Service
public class ReportsService {

	@Autowired 
	private PostLoadRepo postLoadRepo;
	
	
	public List<PostLoad> getMyReportLoads(User sessionUser) {
		List<PostLoad> loads;
		if ("SHIPPER".equalsIgnoreCase(sessionUser.getRole())) {
			loads = postLoadRepo.findByUserId(sessionUser.getId());
		} else {
			loads = postLoadRepo.findByDriverID(sessionUser.getId());
		}

		loads.sort(
				Comparator.comparing(
						PostLoad::getCreatedAt,
						Comparator.nullsLast(Comparator.naturalOrder())
				).reversed()
		);
		return loads;
	}
	
}
