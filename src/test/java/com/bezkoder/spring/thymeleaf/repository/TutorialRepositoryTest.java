package com.bezkoder.spring.thymeleaf.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.bezkoder.spring.thymeleaf.entity.Tutorial;

@DataJpaTest
class TutorialRepositoryTest {

  @Autowired
  private TutorialRepository tutorialRepository;

  @Autowired
  private TestEntityManager entityManager;

  @Test
  void saveAndFindByTitleContaining() {
    Tutorial tutorial = new Tutorial("Spring Boot", "Intro tutorial", 1, true);
    tutorialRepository.save(tutorial);

    assertThat(tutorial.getId()).isNotNull();
    assertThat(tutorialRepository.findByTitleContainingIgnoreCase("spring"))
        .extracting(Tutorial::getTitle)
        .containsExactly("Spring Boot");
  }

  @Test
  void updatePublishedStatus() {
    Tutorial tutorial = tutorialRepository.save(new Tutorial("JPA", "Data access", 2, false));

    tutorialRepository.updatePublishedStatus(tutorial.getId(), true);
    entityManager.clear();

    assertThat(tutorialRepository.findById(tutorial.getId()))
        .get()
        .extracting(Tutorial::isPublished)
        .isEqualTo(true);
  }
}
