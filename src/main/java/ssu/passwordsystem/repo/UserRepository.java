package ssu.passwordsystem.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import ssu.passwordsystem.objects.User;

public interface UserRepository extends JpaRepository<User, String> {
}
