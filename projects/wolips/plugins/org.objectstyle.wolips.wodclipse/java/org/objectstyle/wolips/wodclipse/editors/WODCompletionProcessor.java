package org.objectstyle.wolips.wodclipse.editors;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.objectstyle.wolips.wodclipse.preferences.PreferenceConstants;

public class WODCompletionProcessor implements IContentAssistProcessor {
  private static final String[] FIELD_PREFIXES = { "_" };
  private static final String[] SET_METHOD_PREFIXES = { "set", "_set" };
  private static final String[] GET_METHOD_PREFIXES = { "get", "_get", "is", "_is" };
  private IEditorPart myEditor;
  private Set myValidElementNames;
  private long myTemplateLastModified;

  public WODCompletionProcessor(IEditorPart _editor) {
    myEditor = _editor;
  }

  public char[] getContextInformationAutoActivationCharacters() {
    return null;
  }

  public IContextInformationValidator getContextInformationValidator() {
    return null;
  }

  public IContextInformation[] computeContextInformation(ITextViewer _viewer, int _offset) {
    return null;
  }

  public char[] getCompletionProposalAutoActivationCharacters() {
    return new char[] { ':', '.', '=' };
  }

  public ICompletionProposal[] computeCompletionProposals(ITextViewer _viewer, int _offset) {
    List completionProposalsList = new LinkedList();

    try {
      IDocument document = _viewer.getDocument();
      IEditorInput input = myEditor.getEditorInput();
      if (input instanceof IPathEditorInput) {
        IPathEditorInput pathInput = (IPathEditorInput) input;
        IPath path = pathInput.getPath();
        IResource file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
        IJavaProject javaProject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(file);

        IRegion lineRegion = document.getLineInformationOfOffset(_offset);
        WODScanner scanner = WODScanner.newWODScanner();
        scanner.setRange(document, lineRegion.getOffset(), lineRegion.getLength());
        boolean foundToken = false;
        IRule matchingRule = null;
        while (!foundToken && (matchingRule = scanner.nextMatchingRule()) != null) {
          if (_offset == lineRegion.getOffset() && _offset == scanner.getTokenOffset()) {
            foundToken = true;
          }
          else {
            int tokenEndOffset = scanner.getTokenOffset() + scanner.getTokenLength();
            if (_offset > scanner.getTokenOffset()) {
              if (_offset < tokenEndOffset) {
                foundToken = true;
              }
              else if (_offset == tokenEndOffset) {
                if (matchingRule instanceof OperatorRule) {
                  foundToken = false;
                }
                else {
                  foundToken = true;
                }
              }
            }
          }
        }

        int tokenOffset = scanner.getTokenOffset();
        int tokenLength = scanner.getTokenLength();
        // If you make a completion request in the middle of whitespace, we
        // don't want to select the whitespace, so zero out the 
        // whitespace token offsets.
        if (matchingRule instanceof WhitespaceRule) {
          int partialOffset = (_offset - tokenOffset);
          _offset += partialOffset;
          tokenOffset += partialOffset;
          tokenLength = 0;
        }
        else {
          _viewer.setSelectedRange(_offset, tokenLength - (_offset - tokenOffset));
        }
        String token = document.get(tokenOffset, tokenLength);
        String tokenType = null;
        if (foundToken) {
          if (matchingRule instanceof ElementNameRule) {
            tokenType = PreferenceConstants.ELEMENT_NAME;
          }
          else if (matchingRule instanceof ElementTypeRule) {
            tokenType = PreferenceConstants.ELEMENT_TYPE;
          }
          else if (matchingRule instanceof AssociationNameRule) {
            tokenType = PreferenceConstants.ASSOCIATION_NAME;
          }
          else if (matchingRule instanceof AssociationValueRule) {
            tokenType = PreferenceConstants.ASSOCIATION_VALUE;
          }
          else if (matchingRule instanceof OperatorRule) {
            tokenType = null;//PreferenceConstants.OPERATOR;
          }
          else {
            tokenType = null;
          }
        }

        if (tokenType == null) {
          int startOffset = tokenOffset;
          if (startOffset != 0 && startOffset == document.getLength()) {
            startOffset--;
          }
          int hintChar = -1;
          //for (int startOffset = tokenOffset - 1; tokenType == null && startOffset > 0; startOffset--) {
          for (; tokenType == null && startOffset > 0; startOffset--) {
            int ch = document.getChar(startOffset);
            if (ch == ':') {
              tokenType = PreferenceConstants.ELEMENT_TYPE;
            }
            else if (ch == '{' || ch == ';') {
              tokenType = PreferenceConstants.ASSOCIATION_NAME;
            }
            else if (ch == '=') {
              tokenType = PreferenceConstants.ASSOCIATION_VALUE;
            }
            else if (ch == '}') {
              tokenType = PreferenceConstants.ELEMENT_NAME;
            }
          }
        }

        if (tokenType == null) {
          tokenType = PreferenceConstants.ELEMENT_NAME;
        }

        if (tokenType == PreferenceConstants.ELEMENT_NAME) {
          fillInElementNameCompletionProposals(javaProject, document, path, token, tokenOffset, _offset, completionProposalsList);
        }
        else if (tokenType == PreferenceConstants.ELEMENT_TYPE) {
          fillInElementTypeCompletionProposals(javaProject, token, tokenOffset, _offset, completionProposalsList);
        }
        else if (tokenType == PreferenceConstants.ASSOCIATION_NAME) {
          IType componentType = findElementType(javaProject, document, scanner, tokenOffset);
          fillInAssociationNameCompletionProposals(javaProject, componentType, path, token, tokenOffset, _offset, completionProposalsList);
        }
        else if (tokenType == PreferenceConstants.ASSOCIATION_VALUE) {
          String associatedTypeName = path.removeFileExtension().lastSegment();
          IType associatedType = findElementType(javaProject, associatedTypeName, true);
          fillInAssociationValueCompletionProposals(javaProject, associatedType, token, tokenOffset, _offset, completionProposalsList);
        }
      }
    }
    catch (JavaModelException e) {
      e.printStackTrace();
    }
    catch (BadLocationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (CoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    ICompletionProposal[] completionProposals = new ICompletionProposal[completionProposalsList.size()];
    completionProposalsList.toArray(completionProposals);
    return completionProposals;
  }

  public String getErrorMessage() {
    return null;
  }

  protected Set validComponentNames(IPath _filePath) throws CoreException, IOException {
    IPath templatePath = _filePath.removeFileExtension().addFileExtension("html");
    IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(templatePath);
    //myTemplateLastModified = IFile.NULL_STAMP;
    if (file != null) {
      long templateLastModified = file.getModificationStamp();
      if (myValidElementNames == null || myTemplateLastModified == IFile.NULL_STAMP || templateLastModified > myTemplateLastModified) {
        myValidElementNames = new TreeSet();
        myTemplateLastModified = templateLastModified;
        FileEditorInput fileInput = new FileEditorInput(file);
        InputStream fileContents = fileInput.getStorage().getContents();
        BufferedInputStream bis = new BufferedInputStream(fileContents);
        int ch;

        char[] stringToMatch = { '<', 'w', 'e', 'b', 'o', 'b', 'j', 'e', 'c', 't', 'n', 'a', 'm', 'e', '=' };
        int matchIndex = 0;
        StringBuffer elementNameBuffer = null;
        boolean elementFound = false;
        while ((ch = bis.read()) != -1) {
          if (elementNameBuffer == null) {
            if (ch == ' ') {
              // ignore spaces
            }
            else if (Character.toLowerCase((char) ch) == stringToMatch[matchIndex]) {
              matchIndex++;
              if (matchIndex == stringToMatch.length) {
                elementNameBuffer = new StringBuffer();
                matchIndex = 0;
              }
            }
            else {
              matchIndex = 0;
              if (Character.toLowerCase((char) ch) == stringToMatch[matchIndex]) {
                matchIndex++;
              }
            }
          }
          else {
            if (ch == ' ') {
            }
            else if (ch == '"') {
            }
            else if (ch == '>') {
              String elementName = elementNameBuffer.toString();
              myValidElementNames.add(elementName);
              elementNameBuffer = null;
            }
            else {
              elementNameBuffer.append((char) ch);
            }
          }
        }
      }
    }
    return myValidElementNames;
  }

  protected String partialToken(String _token, int _tokenOffset, int _offset) {
    String partialToken;
    int partialIndex = _offset - _tokenOffset;
    if (partialIndex > _token.length()) {
      partialToken = _token;
    }
    else {
      partialToken = _token.substring(0, _offset - _tokenOffset);
    }
    return partialToken;
  }

  protected void fillInElementNameCompletionProposals(IJavaProject _project, IDocument _document, IPath _wodFilePath, String _token, int _tokenOffset, int _offset, List _completionProposalsList) throws CoreException, IOException {
    String partialToken = partialToken(_token, _tokenOffset, _offset).toLowerCase();
    Iterator validElementNamesIter = validComponentNames(_wodFilePath).iterator();

    // We really need something like the AST ... This is a pretty expensive way to go here.
    HashSet alreadyUsedElementNames = new HashSet();
    try {
      WODScanner scanner = WODScanner.newWODScanner();
      scanner.setRange(_document, 0, _document.getLength());
      IRule matchingRule = null;
      while ((matchingRule = scanner.nextMatchingRule()) != null) {
        if (matchingRule instanceof ElementNameRule) {
          int tokenOffset = scanner.getTokenOffset();
          int tokenLength = scanner.getTokenLength();
          String alreadyUsedElementName = _document.get(tokenOffset, tokenLength);
          alreadyUsedElementNames.add(alreadyUsedElementName);
        }
      }
    }
    catch (Throwable t) {
      // It's not THAT big of a deal ...
      t.printStackTrace();
    }

    while (validElementNamesIter.hasNext()) {
      String validElementName = (String) validElementNamesIter.next();
      if (validElementName.toLowerCase().startsWith(partialToken) && !alreadyUsedElementNames.contains(validElementName)) {
        _completionProposalsList.add(completionProposal(_token, _tokenOffset, _offset, validElementName));
      }
    }
  }

  protected void fillInElementTypeCompletionProposals(IJavaProject _project, String _token, int _tokenOffset, int _offset, List _completionProposalsList) throws JavaModelException {
    TypeNameCollector typeNameCollector = new TypeNameCollector(_project, false);
    String partialToken = partialToken(_token, _tokenOffset, _offset);
    if (partialToken.length() > 0) {
      findMatchingComponentClassNames(partialToken, SearchPattern.R_PREFIX_MATCH, typeNameCollector);
      Iterator matchingComponentClassNamesIter = typeNameCollector.typeNames();
      while (matchingComponentClassNamesIter.hasNext()) {
        String matchingComponentClassName = (String) matchingComponentClassNamesIter.next();
        String matchingComponentTypeName;
        int lastDotIndex = matchingComponentClassName.lastIndexOf('.');
        if (lastDotIndex == -1) {
          matchingComponentTypeName = matchingComponentClassName;
        }
        else {
          matchingComponentTypeName = matchingComponentClassName.substring(lastDotIndex + 1);
        }
        CompletionProposal completionProposal = completionProposal(_token, _tokenOffset, _offset, matchingComponentTypeName);
        _completionProposalsList.add(completionProposal);
      }
    }
  }

  protected void fillInAssociationNameCompletionProposals(IJavaProject _project, IType _componentType, IPath _wodFilePath, String _token, int _tokenOffset, int _offset, List _completionProposalsList) throws JavaModelException {
    String partialToken = partialToken(_token, _tokenOffset, _offset).toLowerCase();

    ITypeHierarchy typeHierarchy = _componentType.newSupertypeHierarchy(null);
    IType[] types = typeHierarchy.getAllTypes();
    for (int typeNum = 0; typeNum < types.length; typeNum++) {
      IField[] fields = types[typeNum].getFields();
      for (int fieldNum = 0; fieldNum < fields.length; fieldNum++) {
        findMemberProposals(fields[fieldNum], partialToken, WODCompletionProcessor.FIELD_PREFIXES, _token, _tokenOffset, _offset, _completionProposalsList, 1, false);
      }

      IMethod[] methods = types[typeNum].getMethods();
      for (int methodNum = 0; methodNum < methods.length; methodNum++) {
        findMemberProposals(methods[methodNum], partialToken, WODCompletionProcessor.SET_METHOD_PREFIXES, _token, _tokenOffset, _offset, _completionProposalsList, 1, false);
      }
    }

    /*
    IOpenable typeContainer = _componentType.getOpenable();
    if (typeContainer instanceof IClassFile) {
      IClassFile classFile = (IClassFile) typeContainer;
      IJavaElement parent = classFile.getParent();
      if (parent instanceof IPackageFragment) {
        IPackageFragment parentPackage = (IPackageFragment) parent;
        IPath packagePath = parentPackage.getPath();
        IPath apiPath = packagePath.removeLastSegments(2).append(_componentType.getElementName()).addFileExtension("api");
        File apiFile = apiPath.toFile();
        System.out.println("WODCompletionProcessor.fillInAssociationNameCompletionProposals: " + apiPath);
        if (apiFile.exists()) {
          System.out.println("WODCompletionProcessor.fillInAssociationNameCompletionProposals: exists");
        }
      }
    }
    else if (typeContainer instanceof ICompilationUnit) {
      ICompilationUnit cu = (ICompilationUnit) typeContainer;
    }
    */
  }

  protected void fillInAssociationValueCompletionProposals(IJavaProject _project, IType _associatedType, String _token, int _tokenOffset, int _offset, List _completionProposalsList) throws JavaModelException {
    String partialToken = partialToken(_token, _tokenOffset, _offset).toLowerCase();
    String[] accessors = partialToken.split("\\.");
    // Split tosses empty tokens, so we check to see if we're on the last "." and fake an empty token in the list
    if (partialToken.length() > 0 && partialToken.charAt(partialToken.length() - 1) == '.') {
      String[] addedBlankAccessor = new String[accessors.length + 1];
      System.arraycopy(accessors, 0, addedBlankAccessor, 0, accessors.length);
      addedBlankAccessor[addedBlankAccessor.length - 1] = "";
      accessors = addedBlankAccessor;
    }
    IType nextType = _associatedType;
    int offset = _tokenOffset;
    for (int i = 0; nextType != null && i < accessors.length - 1; i++) {
      String nextTypeName = nextType(nextType, accessors[i]);
      if (nextTypeName != null) {
        nextTypeName = Signature.toString(nextTypeName);
        IType nextTypeAttempt = _project.findType(nextTypeName);
        if (nextTypeAttempt == null) {
          String[][] resolvedTypes = nextType.resolveType(nextTypeName);
          if (resolvedTypes != null && resolvedTypes.length == 1) {
            nextTypeName = Signature.toQualifiedName(resolvedTypes[0]);
            nextType = _project.findType(nextTypeName);
          }
          else {
            nextType = null;
          }
        }
        else {
          nextType = nextTypeAttempt;
        }
      }
      else {
        nextType = null;
      }
    }
    if (nextType != null) {
      // Jump forward to the last '.'
      int previousTokenLength = partialToken.lastIndexOf('.') + 1;
      _tokenOffset += previousTokenLength;

      String accessor = accessors[accessors.length - 1];
      _token = accessors[accessors.length - 1];
      ITypeHierarchy typeHierarchy = nextType.newSupertypeHierarchy(null);
      IType[] types = typeHierarchy.getAllTypes();
      for (int typeNum = 0; typeNum < types.length; typeNum++) {
        IField[] fields = types[typeNum].getFields();
        for (int fieldNum = 0; fieldNum < fields.length; fieldNum++) {
          findMemberProposals(fields[fieldNum], accessor, WODCompletionProcessor.FIELD_PREFIXES, _token, _tokenOffset, _offset, _completionProposalsList, 0, true);
        }

        IMethod[] methods = types[typeNum].getMethods();
        for (int methodsNum = 0; methodsNum < methods.length; methodsNum++) {
          findMemberProposals(methods[methodsNum], accessor, WODCompletionProcessor.GET_METHOD_PREFIXES, _token, _tokenOffset, _offset, _completionProposalsList, 0, true);
        }
      }
    }
  }

  protected String nextType(IType _currentType, String _accessor) throws JavaModelException {
    String partialToken = _accessor.toLowerCase();
    String nextTypeName = null;
    ITypeHierarchy typeHierarchy = _currentType.newSupertypeHierarchy(null);
    IType[] types = typeHierarchy.getAllTypes();
    for (int typeNum = 0; nextTypeName == null && typeNum < types.length; typeNum++) {
      IField[] fields = types[typeNum].getFields();
      for (int fieldNum = 0; nextTypeName == null && fieldNum < fields.length; fieldNum++) {
        nextTypeName = findMemberProposals(fields[fieldNum], partialToken, WODCompletionProcessor.FIELD_PREFIXES, "", 0, 0, new LinkedList(), 0, true);
      }

      IMethod[] methods = types[typeNum].getMethods();
      for (int methodNum = 0; nextTypeName == null && methodNum < methods.length; methodNum++) {
        nextTypeName = findMemberProposals(methods[methodNum], partialToken, WODCompletionProcessor.GET_METHOD_PREFIXES, "", 0, 0, new LinkedList(), 0, true);
      }
    }
    return nextTypeName;
  }

  protected String findMemberProposals(IMember _member, String _partialToken, String[] _prefixes, String _token, int _tokenOffset, int _offset, List _completionProposalsList, int _requiredParameterCount, boolean _returnValueRequired) throws JavaModelException {
    String nextType = null;
    int flags = _member.getFlags();
    if (!Flags.isStatic(flags) && !Flags.isPrivate(flags)) {
      boolean memberMatches = false;
      if (_member instanceof IMethod) {
        IMethod method = (IMethod) _member;
        if (method.getParameterNames().length == _requiredParameterCount) {
          nextType = method.getReturnType();
          if (_returnValueRequired && nextType != null && !nextType.equals("V")) {
            memberMatches = true;
          }
          else if (!_returnValueRequired && (nextType == null || nextType.equals("V"))) {
            memberMatches = true;
          }
          else {
            nextType = null;
          }
        }
      }
      else {
        nextType = ((IField) _member).getTypeSignature();
        memberMatches = true;
      }

      if (memberMatches) {
        String elementName = _member.getElementName();
        String lowercaseElementName = elementName.toLowerCase();

        String proposalElementName = null;
        for (int prefixNum = 0; proposalElementName == null && prefixNum < _prefixes.length; prefixNum++) {
          if (lowercaseElementName.startsWith(_prefixes[prefixNum])) {
            int prefixLength = _prefixes[prefixNum].length();
            String noPrefixElementName = lowercaseElementName.substring(prefixLength);
            if (noPrefixElementName.startsWith(_partialToken)) {
              proposalElementName = elementName.substring(prefixLength);
              if (proposalElementName.length() > 0) {
                char firstChar = proposalElementName.charAt(0);
                if (Character.isUpperCase(firstChar)) {
                  proposalElementName = Character.toLowerCase(firstChar) + proposalElementName.substring(1);
                }
              }
            }
          }
        }
        if (proposalElementName == null && lowercaseElementName.startsWith(_partialToken)) {
          proposalElementName = elementName;
        }

        if (proposalElementName != null) {
          CompletionProposal completionProposal = completionProposal(_token, _tokenOffset, _offset, proposalElementName);
          _completionProposalsList.add(completionProposal);
        }
        else {
          nextType = null;
        }
      }
    }
    return nextType;
  }

  protected CompletionProposal completionProposal(String _token, int _tokenOffset, int _offset, String _proposal) {
    CompletionProposal completionProposal = new CompletionProposal(_proposal, _tokenOffset, _token.length(), _proposal.length());
    return completionProposal;
  }

  protected IType findElementType(IJavaProject _project, IDocument _document, WODScanner _scanner, int _offset) throws BadLocationException, JavaModelException {
    IType type;
    int colonOffset = _offset;
    if (colonOffset >= _document.getLength()) {
      colonOffset--;
    }
    for (; colonOffset >= 0; colonOffset--) {
      char ch = _document.getChar(colonOffset);
      if (ch == ':') {
        break;
      }
    }
    if (colonOffset != -1) {
      _scanner.setRange(_document, colonOffset, _offset);
      IRule elementTypeRule = null;
      while (!(elementTypeRule instanceof ElementTypeRule) && (elementTypeRule = _scanner.nextMatchingRule()) != null) {
      }
      if (elementTypeRule instanceof ElementTypeRule) {
        String elementTypeName = _document.get(_scanner.getTokenOffset(), _scanner.getTokenLength());
        type = findElementType(_project, elementTypeName, false);
      }
      else {
        // we didn't find a ElementTypeRule
        type = null;
      }
    }
    else {
      // failed colonoscopy
      type = null;
    }
    return type;
  }

  protected IType findElementType(IJavaProject _javaProject, String _elementTypeName, boolean _requireTypeInProject) throws JavaModelException {
    IType type;
    TypeNameCollector typeNameCollector = new TypeNameCollector(_javaProject, _requireTypeInProject);
    findMatchingComponentClassNames(_elementTypeName, SearchPattern.R_EXACT_MATCH, typeNameCollector);
    if (typeNameCollector.isExactMatch()) {
      String matchingComponentClassName = typeNameCollector.firstTypeName();
      type = typeNameCollector.getTypeForClassName(matchingComponentClassName);
    }
    else {
      // there was more than one matching class!  crap!
      type = null;
    }
    return type;
  }

  protected void findMatchingComponentClassNames(String _componentTypeName, int _matchType, TypeNameCollector _typeNameCollector) throws JavaModelException {
    SearchEngine searchEngine = new SearchEngine();
    IJavaSearchScope searchScope = SearchEngine.createWorkspaceScope();
    searchEngine.searchAllTypeNames(null, _componentTypeName.toCharArray(), _matchType /*| SearchPattern.R_CASE_SENSITIVE*/, IJavaSearchConstants.TYPE, searchScope, _typeNameCollector, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
  }
}
