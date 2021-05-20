describe('Manage Item combination Questions Walk-through', () => {
  function validateQuestion(title, content) {
    cy.get('[data-cy="showQuestionDialog"]')
      .should('be.visible')
      .within(($ls) => {
        cy.get('.headline').should('contain', title);
        cy.get('span > p').should('contain', content);
      });
  }

  function validateQuestionFull(title, content) {
    cy.log('Validate question with show dialog.');

    cy.get('[data-cy="questionTitleGrid"]').first().click();

    validateQuestion(title, content);

    cy.get('button').contains('close').click();
  }

  before(() => {
    cy.cleanItemCombinationQuestionsByName('Cypress Question Example');
    cy.cleanMultipleChoiceQuestionsByName('Cypress Question Example');
    cy.cleanCodeFillInQuestionsByName('Cypress Question Example');
  });
  after(() => {
    cy.cleanItemCombinationQuestionsByName('Cypress Question Example');
  });

  beforeEach(() => {
    cy.demoTeacherLogin();
    cy.server();
    cy.route('GET', '/questions/courses/*').as('getQuestions');
    cy.route('GET', '/topics/courses/*').as('getTopics');
    cy.get('[data-cy="managementMenuButton"]').click();
    cy.get('[data-cy="questionsTeacherMenuButton"]').click();

    cy.wait('@getQuestions').its('status').should('eq', 200);

    cy.wait('@getTopics').its('status').should('eq', 200);
  });

  afterEach(() => {
    cy.logout();
  });

  it('Creates a new item combination question', function () {
    cy.get('button').contains('New Question').click();

    cy.get('[data-cy="createOrEditQuestionDialog"]')
      .parent()
      .should('be.visible');

    cy.get('span.headline').should('contain', 'New Question');

    cy.get('[data-cy="questionTypeInput"]')
      .type('item_combination', { force: true })
      .click({ force: true });

    cy.wait(1000);

    cy.get(
      '[data-cy="questionTitleTextArea"]'
    ).type('Cypress Question Example - 01', { force: true });
    cy.get(
      '[data-cy="questionQuestionTextArea"]'
    ).type('Cypress Question Example - Content - 01', { force: true });

    cy.get('[data-cy="addLeftCombinationItem"]').click({ force: true });
    cy.get('[data-cy="addLeftCombinationItem"]').click({ force: true });
    cy.get('[data-cy="questionItemsInputLeft"]')
      .each(($el, index, $list) => {
        cy.get($el).within(($ls) => {
          if (index === 0) {
            cy.get(`[data-cy="Item ${index + 1}"]`).type('DummyItem1');
          }
          if (index === 1) {
            cy.get(`[data-cy="Item ${index + 1}"]`).type('DummyItem2');
          }
        });
      });

    cy.get('[data-cy="addRightCombinationItem"]').click({ force: true });
    cy.get('[data-cy="addRightCombinationItem"]').click({ force: true });

    cy.get('[data-cy="questionItemsInputRight"]')
      .each(($el, index, $list) => {
        cy.get($el).within(($ls) => {
          if (index === 2) {
            cy.get(`[data-cy="Item ${index + 1}"]`).type('DummyItem3');
          }
          if (index === 3) {
            cy.get(`[data-cy="Item ${index + 1}"]`).type('DummyItem4');
          }
        });
      });

    cy.get('[data-cy="Connections 1"]').parent().click();
    cy.get(".v-list-item__title").contains('3').click({ force: true });

    cy.get(".v-list-item__title").contains('4').click({ force: true });

    cy.route('POST', '/questions/courses/*').as('postQuestion');

    cy.get('button').contains('Save').click();

    cy.wait('@postQuestion').its('status').should('eq', 200);

    cy.get('[data-cy="questionTitleGrid"]')
      .first()
      .should('contain', 'Cypress Question Example - 01');

    validateQuestionFull(
      'Cypress Question Example - 01',
      'Cypress Question Example - Content - 01'
    );
  });

  it('Can view question (with button)', function () {
    cy.get('tbody tr')
      .first()
      .within(($list) => {
        cy.get('button').contains('visibility').click();
      });

    validateQuestion(
      'Cypress Question Example - 01',
      'Cypress Question Example - Content - 01'
    );

    cy.get('button').contains('close').click();
  });

  it('Can view question (with click)', function () {
    cy.get('[data-cy="questionTitleGrid"]').first().click();

    validateQuestion(
      'Cypress Question Example - 01',
      'Cypress Question Example - Content - 01'
    );

    cy.get('button').contains('close').click();
  });

  it('Can update title (with right-click)', function () {
    cy.route('PUT', '/questions/*').as('updateQuestion');

    cy.get('[data-cy="questionTitleGrid"]').first().rightclick();

    cy.get('[data-cy="createOrEditQuestionDialog"]')
      .parent()
      .should('be.visible')
      .within(($list) => {
        cy.get('span.headline').should('contain', 'Edit Question');

        cy.get('[data-cy="questionTitleTextArea"]')
          .clear({ force: true })
          .type('Cypress Question Example - 01 - Edited', { force: true });

        cy.get('button').contains('Save').click();
      });

    cy.wait('@updateQuestion').its('status').should('eq', 200);

    cy.get('[data-cy="questionTitleGrid"]')
      .first()
      .should('contain', 'Cypress Question Example - 01 - Edited');

    validateQuestionFull(
      (title = 'Cypress Question Example - 01 - Edited'),
      (content = 'Cypress Question Example - Content - 01')
    );
  });

  it('Can update content (with button)', function () {
    cy.route('PUT', '/questions/*').as('updateQuestion');

    cy.get('tbody tr')
      .first()
      .within(($list) => {
        cy.get('button').contains('edit').click();
      });

    cy.get('[data-cy="createOrEditQuestionDialog"]')
      .parent()
      .should('be.visible')
      .within(($list) => {
        cy.get('span.headline').should('contain', 'Edit Question');

        cy.get('[data-cy="questionQuestionTextArea"]')
          .clear({ force: true })
          .type('Cypress New Content For Question!', { force: true });

        cy.get('button').contains('Save').click();
      });

    cy.wait('@updateQuestion').its('status').should('eq', 200);

    validateQuestionFull(
      (title = 'Cypress Question Example - 01 - Edited'),
      (content = 'Cypress New Content For Question!')
    );
  });

  it('Can update items', function () {
    cy.route('PUT', '/questions/*').as('updateQuestion');

    cy.get('tbody tr')
      .first()
      .within(($list) => {
        cy.get('button').contains('edit').click();
      });

    cy.get('[data-cy="createOrEditQuestionDialog"]')
      .parent()
      .should('be.visible')
      .within(($list) => {
        cy.get('span.headline').should('contain', 'Edit Question');
      });

    cy.get('[data-cy="addRightCombinationItem"]').click({ force: true });
    cy.get('[data-cy="questionItemsInputRight"')
      .each(($el, index, $list) => {
        cy.get($el).within(($ls) => {
          if (index === 3) {
            cy.get(`[data-cy="Item ${index + 1}"]`).clear();
            cy.get(`[data-cy="Item ${index + 1}"]`).type('Changed');
          }
          if (index === 4) {
            cy.get(`[data-cy="Item ${index + 1}"]`).type('New');
          }
        });
      });

    cy.get('button').contains('Save').click();

    cy.wait('@updateQuestion').its('status').should('eq', 200);

    validateQuestionFull(
      (title = 'Cypress Question Example - 01 - Edited'),
      (content = 'Cypress New Content For Question!')
    );
  });

  it('Can update connections', function () {
    cy.route('PUT', '/questions/*').as('updateQuestion');

    cy.get('tbody tr')
      .first()
      .within(($list) => {
        cy.get('button').contains('edit').click();
      });

    cy.get('[data-cy="createOrEditQuestionDialog"]')
      .parent()
      .should('be.visible')
      .within(($list) => {
        cy.get('span.headline').should('contain', 'Edit Question');
      });

    cy.get('[data-cy="Connections 1"]').parent().click();
    cy.get(".v-list-item__title").contains('4').click({ force: true });
    cy.get(".v-list-item__title").contains('5').click({ force: true });


    cy.get('button').contains('Save').click();

    cy.wait('@updateQuestion').its('status').should('eq', 200);

    validateQuestionFull(
      (title = 'Cypress Question Example - 01 - Edited'),
      (content = 'Cypress New Content For Question!')
    );
  });

  it('Can duplicate item combination question', function () {

    cy.get('tbody tr')
      .first()
      .within(($list) => {
        cy.get('button').contains('cached').click();
      });

    cy.get('[data-cy="createOrEditQuestionDialog"]')
      .parent()
      .should('be.visible');

    cy.get('span.headline').should('contain', 'New Question');


    cy.get('[data-cy="questionTitleTextArea"]')
      .should('have.value', 'Cypress Question Example - 01 - Edited')
      .type('{end} - DUP', { force: true });
    cy.get('[data-cy="questionQuestionTextArea"]').should(
      'have.value',
      'Cypress New Content For Question!'
    );

    cy.route('POST', '/questions/courses/*').as('postQuestion');

    cy.get('button').contains('Save').click();

    cy.wait('@postQuestion').its('status').should('eq', 200);

    cy.get('[data-cy="questionTitleGrid"]')
      .first()
      .should('contain', 'Cypress Question Example - 01 - Edited - DUP');

    validateQuestionFull(
      'Cypress Question Example - 01 - Edited - DUP',
      'Cypress New Content For Question!')

  });

  it('Can delete item combination question', function () {
    cy.route('DELETE', '/questions/*').as('deleteQuestion');
    cy.get('tbody tr')
      .first()
      .within(($list) => {
        cy.get('button').contains('delete').click();
      });
    cy.wait('@deleteQuestion').its('status').should('eq', 200);
  });
});
