package com.ichuvilin.entity;


import com.ichuvilin.entity.enums.UserState;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Long telegramUserId;
	@CreationTimestamp
	private LocalDateTime firstLoginDate;
	private String firstName;
	private String lastName;
	private String username;
	@Enumerated(EnumType.STRING)
	private UserState state;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
	private List<Todos> todos;
}
