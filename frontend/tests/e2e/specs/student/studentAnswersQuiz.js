describe('Student answers quizzes created', () => {
  before(() => {
    cy.demoTeacherLogin();
    cy.createQuestion('Single', 'Single Content', 'Option1', 'Option2', 'Option3', 'Option4');
    cy.createMultipleChoiceQuestionMultipleType('Multiple', 'Multiple Content',
      'Option1', 'Option2', 'Option3', 'Option4');
    cy.createMultipleChoiceQuestionOrderType('Order', 'Order Content',
      'Option1', 'Option2', 'Option3', 'Option4');
    cy.createQuizzWith3Questions('MultipleChoiceQuiz', 'Single', 'Multiple', 'Order');

    cy.createOpenEndedQuestionType('Open Ended Title 1', 'Open Ended Question 1',
        'Open Ended Question 1 Criteria', '[0-9]');
    cy.createOpenEndedQuestionType('Open Ended Title 2', 'Open Ended Question 2',
        'Open Ended Question 2 Criteria', 'a');
    cy.createQuizzWith2Questions('OpenEndedQuiz', 'Open Ended Title 1', 'Open Ended Title 2');

    cy.contains('Logout').click();
  });

  after( () => {
    cy.removeMultipleChoiceQuizOptions('MultipleChoiceQuiz');
    cy.removeQuizAnswersAndQuiz('MultipleChoiceQuiz');
    cy.cleanMultipleChoiceQuestionsByName('Single');
    cy.cleanMultipleChoiceQuestionsByName('Multiple');
    cy.cleanMultipleChoiceQuestionsByName('Order');
    cy.removeQuizAnswersAndQuiz('OpenEndedQuiz');
    cy.cleanOpenEndedQuestionsByName('Open Ended Title 1');
    cy.cleanOpenEndedQuestionsByName('Open Ended Title 2');
  })

  beforeEach(() => {
    cy.demoStudentLogin();

    cy.get('[data-cy="quizzesStudentMenuButton"]').click();
    cy.contains('Available').click();
  });

  afterEach(() => {
    cy.contains('Logout').click();
  });

  it('Answer to multiple choice questions quiz', () => {
    cy.solveQuizz('MultipleChoiceQuiz', 3);

    cy.contains('Single Content')
      .parent()
      .should('have.length', 1);
    cy.get('[data-cy="nextQuestionButton"]').click();

    cy.contains('Multiple Content')
      .parent()
      .should('have.length', 1);
    cy.get('[data-cy="nextQuestionButton"]').click();

    cy.contains('Order Content')
      .parent()
      .should('have.length', 1);
  });


  it('Answer to open ended questions quiz', () => {
    cy.solveQuizzOpenEndedQuestion('OpenEndedQuiz', 2);

    cy.get(':nth-child(1) >.v-card__text').should('contain', 'answer');
    cy.get('#display_result > :nth-child(1) > :nth-child(2) > .v-card__text').should('contain', 'Open Ended Question 1 Criteria');
    cy.get('[data-cy="nextQuestionButton"]').click();
    cy.get('#display_result > :nth-child(1) > :nth-child(2) > .v-card__text').should('contain', 'Open Ended Question 2 Criteria');

  });

});

